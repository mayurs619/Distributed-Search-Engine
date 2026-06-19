"""Distributed web crawler that uses Redis for queueing and deduplication.

The crawler fetches pages, extracts links, and forwards content to a Spring Boot
indexing endpoint while maintaining progress in Redis.
"""

import asyncio
import aiohttp
import redis
from bs4 import BeautifulSoup
from urllib.parse import urljoin, urlparse
import json

class DistributedCrawler:
    def __init__(self, seed_urls, max_depth=2, redis_host='localhost', redis_port=6379):
        self.seed_urls = seed_urls
        self.max_depth = max_depth
        self.r = redis.from_url("rediss://default:gQAAAAAAAk8JAAIgcDE4MjhiODlmYWQ2MGQ0N2MyYTMwYzAyOWVhNzM0ZTM1Yw@flying-woodcock-151305.upstash.io:6379", decode_responses=True)
        self.REDIS_QUEUE_KEY = "search_engine:url_queue"
        self.REDIS_VISITED_KEY = "search_engine:visited_urls"

    async def fetch_page(self, session, url):
        """Fetch the HTML content for a single URL asynchronously."""
        try:
            async with session.get(url, timeout=10) as response:
                if response.status == 200:
                    return await response.text()
                return None
        except Exception as e:
            print(f"Error fetching {url}: {e}")
            return None

    def extract_links(self, base_url, html_content):
        """Parse link targets and page text from HTML content."""
        soup = BeautifulSoup(html_content, 'html.parser')
        links = []

        for anchor in soup.find_all('a', href=True):
            href = anchor['href']
            absolute_url = urljoin(base_url, href)
            parsed_url = urlparse(absolute_url)
            clean_url = f"{parsed_url.scheme}://{parsed_url.netloc}{parsed_url.path}"

            if parsed_url.scheme in ['http', 'https']:
                links.append(clean_url)

        title = soup.title.string if soup.title else "No Title"
        raw_text = soup.get_text(separator=' ', strip=True)
        return links, title, raw_text

    async def crawl_worker(self, worker_id, session):
        """Continuously process URL tasks from Redis and index discovered pages."""
        while True:
            queue_item = self.r.lpop(self.REDIS_QUEUE_KEY)
            if not queue_item:
                await asyncio.sleep(1)
                continue

            data = json.loads(queue_item)
            url = data['url']
            depth = data['depth']
            if depth > self.max_depth:
                continue

            if self.r.sadd(self.REDIS_VISITED_KEY, url) == 0:
                continue

            print(f"[Worker-{worker_id}] Crawling: {url} (Depth: {depth})")
            html_content = await self.fetch_page(session, url)
            if not html_content:
                continue

            links, title, raw_text = self.extract_links(url, html_content)
            print(f"[Worker-{worker_id}] Extracted {len(raw_text)} chars from '{title}'")

            safe_title = title[:1000] if title else "No Title"
            payload = {
                "url": url,
                "title": safe_title,
                "content": raw_text
            }

            try:
                async with session.post("http://localhost:8080/api/documents/index", json=payload) as api_response:
                    response_text = await api_response.text()
                    if api_response.status == 200:
                        print(f"[Worker-{worker_id}] {response_text}")
                    else:
                        print(f"[Worker-{worker_id}] Rejected: {response_text}")
            except Exception as e:
                print(f"[Worker-{worker_id}] API Connection Error: {e}")

            for link in links:
                if not self.r.sismember(self.REDIS_VISITED_KEY, link):
                    task_data = json.dumps({"url": link, "depth": depth + 1})
                    self.r.rpush(self.REDIS_QUEUE_KEY, task_data)

    async def run(self, concurrent_workers=5):
        """Initialize seed URLs and launch worker tasks."""
        if self.r.llen(self.REDIS_QUEUE_KEY) == 0:
            print("Queue empty. Initializing Redis queue with seed URLs...")
            for url in self.seed_urls:
                task_data = json.dumps({"url": url, "depth": 0})
                self.r.rpush(self.REDIS_QUEUE_KEY, task_data)

        headers = {
            "User-Agent": "DukeUni-DistributedSearch-Bot/1.0 (Student Project)"
        }

        async with aiohttp.ClientSession() as session:
            workers = [
                asyncio.create_task(self.crawl_worker(i, session))
                for i in range(concurrent_workers)
            ]
            await asyncio.gather(*workers)

if __name__ == "__main__":
    seeds = ["http://quotes.toscrape.com"]
    crawler = DistributedCrawler(seed_urls=seeds, max_depth=2)

    try:
        print("Starting distributed crawler core. Press Ctrl+C to terminate.")
        asyncio.run(crawler.run(concurrent_workers=3))
    except KeyboardInterrupt:
        print("\nCrawler manually stopped. Current state remains safely saved in Redis!")