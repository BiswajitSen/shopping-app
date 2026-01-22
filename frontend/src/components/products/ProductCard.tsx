import { Link } from 'react-router-dom';
import { ShoppingCart, Heart, Eye } from 'lucide-react';
import { Product, ProductStatus } from '../../types';
import { useCartStore } from '../../store/cartStore';
import toast from 'react-hot-toast';

interface ProductCardProps {
  product: Product;
}

export function ProductCard({ product }: ProductCardProps) {
  const { addItem, getItemQuantity } = useCartStore();
  const cartQuantity = getItemQuantity(product.id);
  const isOutOfStock = product.stock === 0 || product.status === ProductStatus.OUT_OF_STOCK;
  const canAddMore = cartQuantity < product.stock;

  const handleAddToCart = (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    
    if (isOutOfStock) {
      toast.error('Product is out of stock');
      return;
    }
    
    if (!canAddMore) {
      toast.error('Maximum quantity reached');
      return;
    }
    
    addItem(product);
    toast.success('Added to cart!');
  };

  return (
    <Link
      to={`/products/${product.id}`}
      className="group card hover:shadow-lg transition-all duration-300"
    >
      {/* Image */}
      <div className="relative aspect-square bg-gray-100 overflow-hidden">
        {product.images?.[0] ? (
          <img
            src={product.images[0]}
            alt={product.name}
            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
            onError={(e) => {
              e.currentTarget.src = `https://placehold.co/400x400/e2e8f0/64748b?text=${encodeURIComponent(product.name.slice(0, 15))}`;
            }}
          />
        ) : (
          <img
            src={`https://placehold.co/400x400/e2e8f0/64748b?text=${encodeURIComponent(product.name.slice(0, 15))}`}
            alt={product.name}
            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
          />
        )}
        
        {/* Out of stock badge */}
        {isOutOfStock && (
          <div className="absolute inset-0 bg-black/50 flex items-center justify-center">
            <span className="bg-white text-gray-900 px-4 py-2 rounded-lg font-medium">
              Out of Stock
            </span>
          </div>
        )}

        {/* Quick actions */}
        <div className="absolute top-3 right-3 flex flex-col gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
          <button
            onClick={(e) => {
              e.preventDefault();
              e.stopPropagation();
            }}
            className="p-2 bg-white rounded-full shadow-md hover:bg-gray-100 transition-colors"
          >
            <Heart className="h-4 w-4 text-gray-600" />
          </button>
          <button
            onClick={(e) => {
              e.preventDefault();
              e.stopPropagation();
            }}
            className="p-2 bg-white rounded-full shadow-md hover:bg-gray-100 transition-colors"
          >
            <Eye className="h-4 w-4 text-gray-600" />
          </button>
        </div>

        {/* Category badge */}
        <div className="absolute top-3 left-3">
          <span className="badge badge-info">{product.category}</span>
        </div>
      </div>

      {/* Content */}
      <div className="p-4">
        <h3 className="font-medium text-gray-900 group-hover:text-primary-600 transition-colors truncate">
          {product.name}
        </h3>
        <p className="text-sm text-gray-500 mt-1 line-clamp-2">
          {product.description}
        </p>
        
        <div className="mt-4 flex items-center justify-between">
          <div>
            <span className="text-lg font-bold text-gray-900">
              ${product.price.toFixed(2)}
            </span>
            {product.stock > 0 && product.stock <= 5 && (
              <p className="text-xs text-orange-600 mt-1">Only {product.stock} left!</p>
            )}
          </div>
          
          <button
            onClick={handleAddToCart}
            disabled={isOutOfStock || !canAddMore}
            className="btn btn-primary py-2 px-3 text-sm disabled:opacity-50"
          >
            <ShoppingCart className="h-4 w-4" />
            {cartQuantity > 0 && (
              <span className="ml-1">({cartQuantity})</span>
            )}
          </button>
        </div>
      </div>
    </Link>
  );
}
