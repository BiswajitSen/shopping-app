import { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { ShoppingCart, Heart, ArrowLeft, Plus, Minus, Truck, Shield, RotateCcw } from 'lucide-react';
import { productsApi } from '../api/products';
import { useCartStore } from '../store/cartStore';
import { PageLoader } from '../components/common/LoadingSpinner';
import { ProductStatus } from '../types';
import toast from 'react-hot-toast';

export function ProductDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [quantity, setQuantity] = useState(1);
  const [selectedImage, setSelectedImage] = useState(0);
  
  const { addItem, getItemQuantity } = useCartStore();

  const { data: product, isLoading, error } = useQuery({
    queryKey: ['product', id],
    queryFn: () => productsApi.getById(id!),
    enabled: !!id,
  });

  if (isLoading) return <PageLoader />;
  
  if (error || !product) {
    return (
      <div className="max-w-7xl mx-auto px-4 py-12 text-center">
        <h2 className="text-2xl font-bold text-gray-900">Product not found</h2>
        <Link to="/products" className="btn btn-primary mt-4">
          <ArrowLeft className="h-4 w-4 mr-2" />
          Back to Products
        </Link>
      </div>
    );
  }

  const cartQuantity = getItemQuantity(product.id);
  const isOutOfStock = product.stock === 0 || product.status === ProductStatus.OUT_OF_STOCK;
  const maxQuantity = product.stock - cartQuantity;

  const handleAddToCart = () => {
    if (isOutOfStock) {
      toast.error('Product is out of stock');
      return;
    }
    
    if (quantity > maxQuantity) {
      toast.error(`Only ${maxQuantity} items available`);
      return;
    }
    
    addItem(product, quantity);
    toast.success(`Added ${quantity} item(s) to cart!`);
    setQuantity(1);
  };

  const placeholderImage = `https://placehold.co/600x600/e2e8f0/64748b?text=${encodeURIComponent(product.name.slice(0, 20))}`;
  const images = product.images?.length ? product.images : [placeholderImage];

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Breadcrumb */}
      <nav className="flex items-center space-x-2 text-sm text-gray-500 mb-8">
        <Link to="/products" className="hover:text-gray-700">Products</Link>
        <span>/</span>
        <Link to={`/products?category=${product.category}`} className="hover:text-gray-700">
          {product.category}
        </Link>
        <span>/</span>
        <span className="text-gray-900">{product.name}</span>
      </nav>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-12">
        {/* Images */}
        <div className="space-y-4">
          {/* Main Image */}
          <div className="aspect-square bg-gray-100 rounded-2xl overflow-hidden">
            <img
              src={images[selectedImage] || placeholderImage}
              alt={product.name}
              className="w-full h-full object-cover"
              onError={(e) => {
                e.currentTarget.src = placeholderImage;
              }}
            />
          </div>

          {/* Thumbnail Images */}
          {images.length > 1 && (
            <div className="flex gap-4 overflow-x-auto pb-2">
              {images.map((image, index) => (
                <button
                  key={index}
                  onClick={() => setSelectedImage(index)}
                  className={`w-20 h-20 rounded-lg overflow-hidden flex-shrink-0 border-2 transition-colors ${
                    selectedImage === index ? 'border-primary-500' : 'border-transparent'
                  }`}
                >
                  <img 
                    src={image || placeholderImage} 
                    alt="" 
                    className="w-full h-full object-cover"
                    onError={(e) => {
                      e.currentTarget.src = placeholderImage;
                    }}
                  />
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Product Info */}
        <div className="space-y-6">
          <div>
            <span className="badge badge-info mb-2">{product.category}</span>
            <h1 className="text-3xl font-bold text-gray-900">{product.name}</h1>
          </div>

          <div className="flex items-baseline space-x-4">
            <span className="text-4xl font-bold text-gray-900">
              ${product.price.toFixed(2)}
            </span>
          </div>

          <p className="text-gray-600 text-lg leading-relaxed">
            {product.description}
          </p>

          {/* Stock Status */}
          <div className="flex items-center space-x-2">
            {isOutOfStock ? (
              <span className="badge badge-danger">Out of Stock</span>
            ) : product.stock <= 5 ? (
              <span className="badge badge-warning">Only {product.stock} left!</span>
            ) : (
              <span className="badge badge-success">In Stock</span>
            )}
            {cartQuantity > 0 && (
              <span className="text-sm text-gray-500">
                ({cartQuantity} in cart)
              </span>
            )}
          </div>

          {/* Quantity & Add to Cart */}
          {!isOutOfStock && (
            <div className="flex items-center space-x-4">
              {/* Quantity Selector */}
              <div className="flex items-center border border-gray-300 rounded-lg">
                <button
                  onClick={() => setQuantity((q) => Math.max(1, q - 1))}
                  className="p-3 hover:bg-gray-100 transition-colors"
                >
                  <Minus className="h-4 w-4" />
                </button>
                <span className="px-4 font-medium">{quantity}</span>
                <button
                  onClick={() => setQuantity((q) => Math.min(maxQuantity, q + 1))}
                  disabled={quantity >= maxQuantity}
                  className="p-3 hover:bg-gray-100 transition-colors disabled:opacity-50"
                >
                  <Plus className="h-4 w-4" />
                </button>
              </div>

              {/* Add to Cart Button */}
              <button
                onClick={handleAddToCart}
                disabled={maxQuantity <= 0}
                className="btn btn-primary flex-1 py-3 text-lg"
              >
                <ShoppingCart className="h-5 w-5 mr-2" />
                Add to Cart
              </button>

              {/* Wishlist Button */}
              <button className="p-3 border border-gray-300 rounded-lg hover:bg-gray-100 transition-colors">
                <Heart className="h-5 w-5" />
              </button>
            </div>
          )}

          {/* Features */}
          <div className="border-t pt-6 space-y-4">
            <div className="flex items-center space-x-3 text-gray-600">
              <Truck className="h-5 w-5" />
              <span>Free shipping on orders over $50</span>
            </div>
            <div className="flex items-center space-x-3 text-gray-600">
              <Shield className="h-5 w-5" />
              <span>Secure payment guaranteed</span>
            </div>
            <div className="flex items-center space-x-3 text-gray-600">
              <RotateCcw className="h-5 w-5" />
              <span>30-day return policy</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
