"use client";

import { useState } from "react";

interface SearchResult {
  url: string;
  title: string;
  score: number;
}

export default function Home() {
  const [query, setQuery] = useState("");
  const [results, setResults] = useState<SearchResult[]>([]);
  const [hasSearched, setHasSearched] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!query.trim()) return;

    setLoading(true);
    setHasSearched(true);

    try {
      const res = await fetch(`http://localhost:8080/api/documents/search?q=${encodeURIComponent(query)}`);
      const data = await res.json();
      setResults(data);
    } catch (error) {
      console.error("Error fetching results:", error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 text-gray-900 font-sans">
      {/* Header / Search Bar Area */}
      <main className={`flex flex-col items-center transition-all duration-500 ease-in-out ${hasSearched ? "pt-12" : "pt-64"}`}>
        {!hasSearched && (
          <h1 className="text-6xl font-bold tracking-tighter mb-8 text-blue-600">
            Distributed<span className="text-gray-800">Search</span>
          </h1>
        )}

        <form onSubmit={handleSearch} className="w-full max-w-2xl px-4 relative">
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search the index..."
            className="w-full px-6 py-4 text-lg border border-gray-300 rounded-full shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-shadow"
          />
          <button 
            type="submit" 
            className="absolute right-6 top-3 bg-blue-600 text-white px-6 py-2 rounded-full font-medium hover:bg-blue-700 transition-colors"
          >
            Search
          </button>
        </form>
      </main>

      {/* Results Area */}
      {hasSearched && (
        <div className="max-w-4xl mx-auto mt-12 px-4 pb-12">
          {loading ? (
            <p className="text-gray-500 text-lg">Querying distributed shards...</p>
          ) : (
            <div className="space-y-8">
              <p className="text-sm text-gray-500">
                Found {results.length} results sorted by TF-IDF relevance.
              </p>
              
              {results.length === 0 ? (
                <p className="text-xl text-gray-700">No results found for "{query}".</p>
              ) : (
                results.map((result, idx) => {
                  // Extract the [SHARDX] tag to style it beautifully
                  const isShard1 = result.title.includes("[SHARD1]");
                  const cleanTitle = result.title.replace(/\[SHARD[12]\]\s*/, "");

                  return (
                    <div key={idx} className="group">
                      <div className="flex items-center gap-2 mb-1">
                        <span className={`text-xs font-bold px-2 py-1 rounded ${isShard1 ? 'bg-purple-100 text-purple-700' : 'bg-orange-100 text-orange-700'}`}>
                          {isShard1 ? 'SHARD 1' : 'SHARD 2'}
                        </span>
                        <span className="text-sm text-gray-500 truncate max-w-lg">{result.url}</span>
                      </div>
                      <a href={result.url} target="_blank" rel="noopener noreferrer" className="text-xl font-medium text-blue-700 group-hover:underline">
                        {cleanTitle}
                      </a>
                      <p className="text-sm text-gray-600 mt-1">
                        TF-IDF Relevance Score: <span className="font-mono bg-gray-100 px-1">{result.score.toFixed(4)}</span>
                      </p>
                    </div>
                  );
                })
              )}
            </div>
          )}
        </div>
      )}
    </div>
  );
}