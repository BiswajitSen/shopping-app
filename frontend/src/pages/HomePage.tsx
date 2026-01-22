import { Link } from 'react-router-dom';
import { ArrowRight, ShoppingBag, Truck, Shield, Star, Store } from 'lucide-react';
import { useAuthStore } from '../store/authStore';
import { UserRole } from '../types';

export function HomePage() {
  const { isAuthenticated, user } = useAuthStore();
  const isVendor = user?.roles?.includes(UserRole.VENDOR);
  const categories = [
    { name: 'Electronics', image: '🔌', count: 150 },
    { name: 'Clothing', image: '👕', count: 320 },
    { name: 'Home & Garden', image: '🏠', count: 85 },
    { name: 'Sports', image: '⚽', count: 120 },
    { name: 'Books', image: '📚', count: 200 },
    { name: 'Toys', image: '🎮', count: 95 },
  ];

  const features = [
    {
      icon: ShoppingBag,
      title: 'Wide Selection',
      description: 'Browse thousands of products from verified vendors',
    },
    {
      icon: Truck,
      title: 'Fast Shipping',
      description: 'Quick delivery right to your doorstep',
    },
    {
      icon: Shield,
      title: 'Secure Payments',
      description: 'Your transactions are always protected',
    },
    {
      icon: Star,
      title: 'Quality Guaranteed',
      description: 'All vendors are verified for quality assurance',
    },
  ];

  return (
    <div>
      {/* Hero Section */}
      <section className="relative bg-gradient-to-br from-primary-600 via-primary-700 to-primary-800 text-white">
        <div className="absolute inset-0 bg-black/10" />
        <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24 lg:py-32">
          <div className="max-w-2xl">
            <h1 className="text-4xl md:text-5xl lg:text-6xl font-bold leading-tight">
              Discover Amazing Products from{' '}
              <span className="text-primary-200">Trusted Vendors</span>
            </h1>
            <p className="mt-6 text-lg text-primary-100 max-w-lg">
              Shop from thousands of products across multiple categories. Quality assured, 
              fast delivery, and secure payments.
            </p>
            <div className="mt-8 flex flex-col sm:flex-row gap-4">
              <Link
                to="/products"
                className="btn bg-white text-primary-700 hover:bg-primary-50 px-6 py-3 text-lg"
              >
                Start Shopping
                <ArrowRight className="ml-2 h-5 w-5" />
              </Link>
              <Link
                to={isVendor ? "/vendor/dashboard" : "/vendor/register"}
                className="btn bg-transparent border-2 border-white text-white hover:bg-white/10 px-6 py-3 text-lg"
              >
                {isVendor ? "Vendor Dashboard" : "Become a Vendor"}
              </Link>
            </div>
          </div>
        </div>
        
        {/* Decorative shapes */}
        <div className="absolute bottom-0 left-0 right-0 h-16 bg-gray-50 rounded-t-[50px]" />
      </section>

      {/* Features Section */}
      <section className="py-16 bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
            {features.map((feature, index) => (
              <div
                key={index}
                className="bg-white rounded-xl p-6 shadow-sm border border-gray-100 hover:shadow-md transition-shadow"
              >
                <div className="w-12 h-12 bg-primary-100 rounded-lg flex items-center justify-center mb-4">
                  <feature.icon className="h-6 w-6 text-primary-600" />
                </div>
                <h3 className="text-lg font-semibold text-gray-900 mb-2">{feature.title}</h3>
                <p className="text-gray-600 text-sm">{feature.description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Categories Section */}
      <section className="py-16">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-12">
            <h2 className="text-3xl font-bold text-gray-900">Shop by Category</h2>
            <p className="mt-2 text-gray-600">Browse our wide range of categories</p>
          </div>

          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
            {categories.map((category, index) => (
              <Link
                key={index}
                to={`/products?category=${category.name.toLowerCase()}`}
                className="group bg-white rounded-xl p-6 shadow-sm border border-gray-100 hover:shadow-md hover:border-primary-200 transition-all text-center"
              >
                <div className="text-4xl mb-3">{category.image}</div>
                <h3 className="font-medium text-gray-900 group-hover:text-primary-600 transition-colors">
                  {category.name}
                </h3>
                <p className="text-sm text-gray-500 mt-1">{category.count} items</p>
              </Link>
            ))}
          </div>
        </div>
      </section>

      {/* CTA Section */}
      {/* Vendor CTA Section - Only show for non-vendors */}
      {!isVendor && (
        <section className="py-16 bg-gray-900 text-white">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="flex flex-col lg:flex-row items-center justify-between gap-8">
              <div className="text-center lg:text-left">
                <div className="flex items-center justify-center lg:justify-start space-x-2 mb-4">
                  <Store className="h-8 w-8 text-primary-400" />
                  <span className="text-xl font-bold">Start Selling Today</span>
                </div>
                <h2 className="text-3xl md:text-4xl font-bold mb-4">
                  Become a Vendor and Grow Your Business
                </h2>
                <p className="text-gray-400 max-w-lg">
                  Join our marketplace and reach thousands of customers. Easy setup, 
                  powerful tools, and dedicated support to help you succeed.
                </p>
              </div>
              <Link
                to="/vendor/register"
                className="btn bg-primary-600 hover:bg-primary-700 text-white px-8 py-4 text-lg flex-shrink-0"
              >
                Register as Vendor
                <ArrowRight className="ml-2 h-5 w-5" />
              </Link>
            </div>
          </div>
        </section>
      )}

      {/* Stats Section */}
      <section className="py-16 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-8 text-center">
            <div>
              <div className="text-4xl font-bold text-primary-600">10K+</div>
              <div className="text-gray-600 mt-1">Products</div>
            </div>
            <div>
              <div className="text-4xl font-bold text-primary-600">500+</div>
              <div className="text-gray-600 mt-1">Vendors</div>
            </div>
            <div>
              <div className="text-4xl font-bold text-primary-600">50K+</div>
              <div className="text-gray-600 mt-1">Happy Customers</div>
            </div>
            <div>
              <div className="text-4xl font-bold text-primary-600">99%</div>
              <div className="text-gray-600 mt-1">Satisfaction Rate</div>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
