import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Search, Filter, ChevronDown, X, Grid, List } from 'lucide-react';
import { useQuery } from '@tanstack/react-query';
import { productsApi } from '../api/products';
import { ProductCard } from '../components/products/ProductCard';
import { PageLoader } from '../components/common/LoadingSpinner';

const categories = [
  'All',
  'Electronics',
  'Clothing',
  'Home & Garden',
  'Sports',
  'Books',
  'Toys',
];

const sortOptions = [
  { value: 'newest', label: 'Newest First' },
  { value: 'price_low', label: 'Price: Low to High' },
  { value: 'price_high', label: 'Price: High to Low' },
  { value: 'popular', label: 'Most Popular' },
];

export function ProductsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [searchQuery, setSearchQuery] = useState(searchParams.get('q') || '');
  const [selectedCategory, setSelectedCategory] = useState(searchParams.get('category') || 'All');
  const [sortBy, setSortBy] = useState('newest');
  const [page, setPage] = useState(0);
  const [showFilters, setShowFilters] = useState(false);

  const { data, isLoading, error } = useQuery({
    queryKey: ['products', page, selectedCategory, searchQuery],
    queryFn: () => productsApi.getAll(page, 12, selectedCategory === 'All' ? undefined : selectedCategory),
  });

  useEffect(() => {
    const params = new URLSearchParams();
    if (selectedCategory !== 'All') params.set('category', selectedCategory);
    if (searchQuery) params.set('q', searchQuery);
    setSearchParams(params);
  }, [selectedCategory, searchQuery, setSearchParams]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(0);
  };

  const handleCategoryChange = (category: string) => {
    setSelectedCategory(category);
    setPage(0);
  };

  const clearFilters = () => {
    setSelectedCategory('All');
    setSearchQuery('');
    setPage(0);
  };

  const hasActiveFilters = selectedCategory !== 'All' || searchQuery;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Products</h1>
          <p className="text-gray-600 mt-1">
            {data?.totalElements || 0} products found
          </p>
        </div>

        {/* Search */}
        <form onSubmit={handleSearch} className="flex-1 max-w-md">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" />
            <input
              type="text"
              placeholder="Search products..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="input pl-10 pr-4"
            />
          </div>
        </form>
      </div>

      {/* Filters Bar */}
      <div className="flex flex-wrap items-center gap-4 mb-6">
        {/* Mobile Filter Toggle */}
        <button
          onClick={() => setShowFilters(!showFilters)}
          className="md:hidden btn btn-secondary"
        >
          <Filter className="h-4 w-4 mr-2" />
          Filters
        </button>

        {/* Category Pills - Desktop */}
        <div className="hidden md:flex flex-wrap gap-2">
          {categories.map((category) => (
            <button
              key={category}
              onClick={() => handleCategoryChange(category)}
              className={`px-4 py-2 rounded-full text-sm font-medium transition-colors ${
                selectedCategory === category
                  ? 'bg-primary-600 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              {category}
            </button>
          ))}
        </div>

        {/* Sort */}
        <div className="ml-auto flex items-center gap-2">
          <span className="text-sm text-gray-600">Sort by:</span>
          <div className="relative">
            <select
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value)}
              className="input py-2 pr-10 appearance-none cursor-pointer"
            >
              {sortOptions.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
            <ChevronDown className="absolute right-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400 pointer-events-none" />
          </div>
        </div>
      </div>

      {/* Mobile Filters Dropdown */}
      {showFilters && (
        <div className="md:hidden bg-white rounded-lg shadow-lg p-4 mb-6">
          <h3 className="font-medium text-gray-900 mb-3">Categories</h3>
          <div className="flex flex-wrap gap-2">
            {categories.map((category) => (
              <button
                key={category}
                onClick={() => {
                  handleCategoryChange(category);
                  setShowFilters(false);
                }}
                className={`px-3 py-1.5 rounded-full text-sm transition-colors ${
                  selectedCategory === category
                    ? 'bg-primary-600 text-white'
                    : 'bg-gray-100 text-gray-700'
                }`}
              >
                {category}
              </button>
            ))}
          </div>
        </div>
      )}

      {/* Active Filters */}
      {hasActiveFilters && (
        <div className="flex items-center gap-2 mb-6">
          <span className="text-sm text-gray-600">Active filters:</span>
          {selectedCategory !== 'All' && (
            <span className="badge badge-info flex items-center gap-1">
              {selectedCategory}
              <button onClick={() => setSelectedCategory('All')}>
                <X className="h-3 w-3" />
              </button>
            </span>
          )}
          {searchQuery && (
            <span className="badge badge-info flex items-center gap-1">
              "{searchQuery}"
              <button onClick={() => setSearchQuery('')}>
                <X className="h-3 w-3" />
              </button>
            </span>
          )}
          <button
            onClick={clearFilters}
            className="text-sm text-primary-600 hover:text-primary-700"
          >
            Clear all
          </button>
        </div>
      )}

      {/* Products Grid */}
      {isLoading ? (
        <PageLoader />
      ) : error ? (
        <div className="text-center py-12">
          <p className="text-red-600">Failed to load products. Please try again.</p>
        </div>
      ) : data?.content.length === 0 ? (
        <div className="text-center py-12">
          <Grid className="h-16 w-16 text-gray-300 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900">No products found</h3>
          <p className="text-gray-600 mt-1">Try adjusting your search or filters</p>
          {hasActiveFilters && (
            <button
              onClick={clearFilters}
              className="btn btn-primary mt-4"
            >
              Clear Filters
            </button>
          )}
        </div>
      ) : (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {data?.content.map((product) => (
              <ProductCard key={product.id} product={product} />
            ))}
          </div>

          {/* Pagination */}
          {data && data.totalPages > 1 && (
            <div className="flex items-center justify-center gap-2 mt-12">
              <button
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={data.first}
                className="btn btn-secondary disabled:opacity-50"
              >
                Previous
              </button>
              <span className="px-4 text-sm text-gray-600">
                Page {data.page + 1} of {data.totalPages}
              </span>
              <button
                onClick={() => setPage((p) => p + 1)}
                disabled={data.last}
                className="btn btn-secondary disabled:opacity-50"
              >
                Next
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
