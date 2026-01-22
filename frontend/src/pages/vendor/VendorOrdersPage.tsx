import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { 
  Package, Clock, CheckCircle, XCircle, Truck, AlertCircle, 
  Calendar, ChevronDown, ChevronUp, MapPin, User, Phone
} from 'lucide-react';
import { vendorOrdersApi } from '../../api/vendorOrders';
import { useAuthStore } from '../../store/authStore';
import { PageLoader } from '../../components/common/LoadingSpinner';
import { Order, OrderStatus, UpdateOrderStatusRequest, UserRole } from '../../types';
import toast from 'react-hot-toast';

// Status configuration with colors and icons
const statusConfig: Record<string, { label: string; color: string; bgColor: string; icon: typeof Package }> = {
  PLACED: { label: 'Placed', color: 'text-blue-600', bgColor: 'bg-blue-100', icon: Package },
  CREATED: { label: 'Placed', color: 'text-blue-600', bgColor: 'bg-blue-100', icon: Package },
  PREPARING: { label: 'Preparing', color: 'text-yellow-600', bgColor: 'bg-yellow-100', icon: Clock },
  SHIPPED: { label: 'Shipped', color: 'text-purple-600', bgColor: 'bg-purple-100', icon: Truck },
  OUT_FOR_DELIVERY: { label: 'Out for Delivery', color: 'text-orange-600', bgColor: 'bg-orange-100', icon: Truck },
  DELIVERY_SCHEDULED: { label: 'Delivery Scheduled', color: 'text-indigo-600', bgColor: 'bg-indigo-100', icon: Calendar },
  DELIVERED: { label: 'Delivered', color: 'text-green-600', bgColor: 'bg-green-100', icon: CheckCircle },
  CANCELLED: { label: 'Cancelled', color: 'text-red-600', bgColor: 'bg-red-100', icon: XCircle },
  CONFIRMED: { label: 'Confirmed', color: 'text-green-600', bgColor: 'bg-green-100', icon: CheckCircle },
};

// Available status transitions for vendor
const getNextStatuses = (currentStatus: OrderStatus | string): OrderStatus[] => {
  switch (currentStatus) {
    case OrderStatus.PLACED:
    case OrderStatus.CREATED:
    case 'PLACED':
    case 'CREATED':
      return [OrderStatus.PREPARING, OrderStatus.CANCELLED];
    case OrderStatus.CONFIRMED:
    case 'CONFIRMED':
    case OrderStatus.PREPARING:
    case 'PREPARING':
      return [OrderStatus.SHIPPED, OrderStatus.DELIVERY_SCHEDULED, OrderStatus.CANCELLED];
    case OrderStatus.DELIVERY_SCHEDULED:
    case 'DELIVERY_SCHEDULED':
      return [OrderStatus.SHIPPED, OrderStatus.OUT_FOR_DELIVERY];
    case OrderStatus.SHIPPED:
    case 'SHIPPED':
      return [OrderStatus.OUT_FOR_DELIVERY, OrderStatus.DELIVERY_SCHEDULED];
    case OrderStatus.OUT_FOR_DELIVERY:
    case 'OUT_FOR_DELIVERY':
      return [OrderStatus.DELIVERED, OrderStatus.DELIVERY_SCHEDULED];
    case OrderStatus.PENDING:
    case 'PENDING':
    case OrderStatus.PROCESSING:
    case 'PROCESSING':
      return [OrderStatus.PREPARING, OrderStatus.SHIPPED, OrderStatus.CANCELLED];
    default:
      return [];
  }
};

function OrderCard({ order, onStatusUpdate }: { order: Order; onStatusUpdate: () => void }) {
  const [expanded, setExpanded] = useState(false);
  const [selectedStatus, setSelectedStatus] = useState<OrderStatus | ''>('');
  const [estimatedDate, setEstimatedDate] = useState('');
  
  const queryClient = useQueryClient();
  
  const updateStatusMutation = useMutation({
    mutationFn: (request: UpdateOrderStatusRequest) => 
      vendorOrdersApi.updateStatus(order.id, request),
    onSuccess: () => {
      toast.success('Order status updated successfully');
      queryClient.invalidateQueries({ queryKey: ['vendorOrders'] });
      setSelectedStatus('');
      setEstimatedDate('');
      onStatusUpdate();
    },
    onError: (error: Error) => {
      toast.error(error.message || 'Failed to update status');
    },
  });

  const handleStatusChange = (newStatus: OrderStatus) => {
    setSelectedStatus(newStatus);
    
    // If not delivery scheduled, update immediately
    if (newStatus !== OrderStatus.DELIVERY_SCHEDULED) {
      updateStatusMutation.mutate({
        status: newStatus,
      });
    }
  };

  const handleScheduleDelivery = () => {
    if (!estimatedDate) {
      toast.error('Please select an estimated delivery date');
      return;
    }
    
    updateStatusMutation.mutate({
      status: OrderStatus.DELIVERY_SCHEDULED,
      estimatedDeliveryDate: estimatedDate,
    });
  };

  const config = statusConfig[order.status] || statusConfig.PLACED;
  const StatusIcon = config.icon;
  const nextStatuses = getNextStatuses(order.status);
  const canUpdate = nextStatuses.length > 0;

  return (
    <div className="card mb-4">
      <div className="p-6">
        {/* Header */}
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center space-x-3">
            <div className={`p-2 rounded-lg ${config.bgColor}`}>
              <StatusIcon className={`h-5 w-5 ${config.color}`} />
            </div>
            <div>
              <h3 className="font-semibold text-gray-900">Order #{order.id.slice(-8)}</h3>
              <p className="text-sm text-gray-500">
                {new Date(order.createdAt).toLocaleString()}
              </p>
            </div>
          </div>
          <div className="flex items-center space-x-3">
            <span className={`px-3 py-1 rounded-full text-sm font-medium ${config.bgColor} ${config.color}`}>
              {config.label}
            </span>
            <button
              onClick={() => setExpanded(!expanded)}
              className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
            >
              {expanded ? <ChevronUp className="h-5 w-5" /> : <ChevronDown className="h-5 w-5" />}
            </button>
          </div>
        </div>

        {/* Quick Info */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
          <div>
            <p className="text-sm text-gray-500">Items</p>
            <p className="font-medium">{order.items?.length || 0} items</p>
          </div>
          <div>
            <p className="text-sm text-gray-500">Total</p>
            <p className="font-medium text-primary-600">${order.totalAmount?.toFixed(2)}</p>
          </div>
          {order.estimatedDeliveryDate && (
            <div>
              <p className="text-sm text-gray-500">Est. Delivery</p>
              <p className="font-medium">{new Date(order.estimatedDeliveryDate).toLocaleDateString()}</p>
            </div>
          )}
          {order.statusNote && (
            <div>
              <p className="text-sm text-gray-500">Note</p>
              <p className="font-medium text-sm">{order.statusNote}</p>
            </div>
          )}
        </div>

        {/* Quick Status Update Dropdown */}
        {canUpdate && (
          <div className="bg-gray-50 rounded-lg p-4 mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Update Status
            </label>
            <div className="flex flex-wrap gap-2">
              {nextStatuses.map((status) => (
                <button
                  key={status}
                  onClick={() => handleStatusChange(status)}
                  disabled={updateStatusMutation.isPending}
                  className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors
                    ${status === OrderStatus.CANCELLED 
                      ? 'bg-red-100 text-red-700 hover:bg-red-200' 
                      : status === OrderStatus.DELIVERED
                      ? 'bg-green-100 text-green-700 hover:bg-green-200'
                      : status === OrderStatus.DELIVERY_SCHEDULED
                      ? 'bg-indigo-100 text-indigo-700 hover:bg-indigo-200'
                      : 'bg-blue-100 text-blue-700 hover:bg-blue-200'
                    }
                    ${updateStatusMutation.isPending ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer'}
                  `}
                >
                  {statusConfig[status]?.label || status}
                </button>
              ))}
            </div>
            
            {/* Date picker for Delivery Scheduled */}
            {selectedStatus === OrderStatus.DELIVERY_SCHEDULED && (
              <div className="mt-4 p-4 bg-indigo-50 rounded-xl border-2 border-indigo-200 shadow-sm">
                <div className="flex items-center mb-3">
                  <Calendar className="h-5 w-5 text-indigo-600 mr-2" />
                  <label className="text-sm font-semibold text-indigo-800">
                    Select Estimated Delivery Date
                  </label>
                  <span className="text-red-500 ml-1">*</span>
                </div>
                <div className="flex flex-col sm:flex-row gap-3">
                  <input
                    type="date"
                    value={estimatedDate}
                    onChange={(e) => setEstimatedDate(e.target.value)}
                    min={new Date().toISOString().split('T')[0]}
                    className="flex-1 px-4 py-3 border-2 border-indigo-300 rounded-lg text-gray-800 
                               bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500 
                               focus:border-indigo-500 text-base"
                  />
                  <div className="flex gap-2">
                    <button
                      onClick={handleScheduleDelivery}
                      disabled={updateStatusMutation.isPending || !estimatedDate}
                      className="flex-1 sm:flex-none px-6 py-3 bg-indigo-600 text-white font-medium 
                                 rounded-lg hover:bg-indigo-700 disabled:opacity-50 
                                 disabled:cursor-not-allowed transition-colors"
                    >
                      {updateStatusMutation.isPending ? 'Saving...' : 'Confirm'}
                    </button>
                    <button
                      onClick={() => {
                        setSelectedStatus('');
                        setEstimatedDate('');
                      }}
                      className="flex-1 sm:flex-none px-6 py-3 bg-gray-200 text-gray-700 font-medium 
                                 rounded-lg hover:bg-gray-300 transition-colors"
                    >
                      Cancel
                    </button>
                  </div>
                </div>
              </div>
            )}
          </div>
        )}

        {/* Expanded Details */}
        {expanded && (
          <div className="border-t pt-4 space-y-4">
            {/* Order Items */}
            <div>
              <h4 className="font-medium mb-2">Order Items</h4>
              <div className="space-y-2">
                {order.items?.map((item, index) => {
                  const placeholderImage = `https://placehold.co/64x64/e2e8f0/64748b?text=${encodeURIComponent((item.productName || 'Item').slice(0, 5))}`;
                  return (
                    <div key={index} className="flex items-center space-x-3 p-2 bg-gray-50 rounded-lg">
                      <img
                        src={item.productImage || placeholderImage}
                        alt={item.productName}
                        className="w-12 h-12 rounded-lg object-cover"
                        onError={(e) => { e.currentTarget.src = placeholderImage; }}
                      />
                      <div className="flex-1">
                        <p className="font-medium">{item.productName}</p>
                        <p className="text-sm text-gray-500">
                          Qty: {item.quantity} × ${item.unitPrice?.toFixed(2)}
                        </p>
                      </div>
                      <p className="font-medium">${item.subtotal?.toFixed(2)}</p>
                    </div>
                  );
                })}
              </div>
            </div>

            {/* Shipping Address */}
            {order.shippingAddress && (
              <div>
                <h4 className="font-medium mb-2 flex items-center">
                  <MapPin className="h-4 w-4 mr-1" /> Shipping Address
                </h4>
                <div className="bg-gray-50 rounded-lg p-3 text-sm">
                  <p className="flex items-center">
                    <User className="h-4 w-4 mr-2 text-gray-400" />
                    {order.shippingAddress.fullName}
                  </p>
                  <p className="mt-1">{order.shippingAddress.addressLine1}</p>
                  {order.shippingAddress.addressLine2 && (
                    <p>{order.shippingAddress.addressLine2}</p>
                  )}
                  <p>
                    {order.shippingAddress.city}, {order.shippingAddress.state} {order.shippingAddress.postalCode}
                  </p>
                  <p>{order.shippingAddress.country}</p>
                  {order.shippingAddress.phoneNumber && (
                    <p className="flex items-center mt-2">
                      <Phone className="h-4 w-4 mr-2 text-gray-400" />
                      {order.shippingAddress.phoneNumber}
                    </p>
                  )}
                </div>
              </div>
            )}

            {/* Timeline */}
            <div>
              <h4 className="font-medium mb-2">Order Timeline</h4>
              <div className="space-y-2 text-sm">
                <div className="flex justify-between">
                  <span className="text-gray-500">Placed</span>
                  <span>{new Date(order.createdAt).toLocaleString()}</span>
                </div>
                {order.confirmedAt && (
                  <div className="flex justify-between">
                    <span className="text-gray-500">Confirmed</span>
                    <span>{new Date(order.confirmedAt).toLocaleString()}</span>
                  </div>
                )}
                {order.shippedAt && (
                  <div className="flex justify-between">
                    <span className="text-gray-500">Shipped</span>
                    <span>{new Date(order.shippedAt).toLocaleString()}</span>
                  </div>
                )}
                {order.deliveredAt && (
                  <div className="flex justify-between">
                    <span className="text-gray-500">Delivered</span>
                    <span>{new Date(order.deliveredAt).toLocaleString()}</span>
                  </div>
                )}
                {order.cancelledAt && (
                  <div className="flex justify-between text-red-600">
                    <span>Cancelled</span>
                    <span>{new Date(order.cancelledAt).toLocaleString()}</span>
                  </div>
                )}
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export function VendorOrdersPage() {
  const { isAuthenticated, user } = useAuthStore();
  const [statusFilter, setStatusFilter] = useState<OrderStatus | 'ALL'>('ALL');
  
  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['vendorOrders', statusFilter],
    queryFn: () => vendorOrdersApi.getOrders(0, 50, statusFilter === 'ALL' ? undefined : statusFilter),
    enabled: isAuthenticated && user?.roles?.includes(UserRole.VENDOR),
    // Auto-refresh every 15 seconds
    refetchInterval: 15000,
    refetchOnWindowFocus: true,
  });

  if (!isAuthenticated) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-8 text-center">
        <AlertCircle className="h-12 w-12 text-yellow-500 mx-auto mb-4" />
        <h1 className="text-2xl font-bold text-gray-900 mb-2">Sign In Required</h1>
        <p className="text-gray-600">Please sign in to view your orders.</p>
      </div>
    );
  }

  if (!user?.roles?.includes(UserRole.VENDOR)) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-8 text-center">
        <AlertCircle className="h-12 w-12 text-red-500 mx-auto mb-4" />
        <h1 className="text-2xl font-bold text-gray-900 mb-2">Access Denied</h1>
        <p className="text-gray-600">You need to be a vendor to access this page.</p>
      </div>
    );
  }

  if (isLoading) return <PageLoader />;

  if (error) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-8 text-center">
        <AlertCircle className="h-12 w-12 text-red-500 mx-auto mb-4" />
        <h1 className="text-2xl font-bold text-gray-900 mb-2">Error Loading Orders</h1>
        <p className="text-gray-600">{(error as Error).message}</p>
        <button onClick={() => refetch()} className="btn btn-primary mt-4">
          Try Again
        </button>
      </div>
    );
  }

  const orders = data?.content || [];

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <div className="mb-8">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Vendor Orders</h1>
            <p className="text-gray-600 mt-1">Manage and update your customer orders</p>
          </div>
          <div className="flex items-center space-x-2 text-sm text-gray-500">
            <span className="relative flex h-2 w-2">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>
              <span className="relative inline-flex rounded-full h-2 w-2 bg-green-500"></span>
            </span>
            <span>Live</span>
          </div>
        </div>
      </div>

      {/* Status Filter */}
      <div className="mb-6">
        <label className="block text-sm font-medium text-gray-700 mb-2">Filter by Status</label>
        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value as OrderStatus | 'ALL')}
          className="input w-full md:w-64"
        >
          <option value="ALL">All Orders</option>
          <option value={OrderStatus.PLACED}>Placed</option>
          <option value={OrderStatus.PREPARING}>Preparing</option>
          <option value={OrderStatus.SHIPPED}>Shipped</option>
          <option value={OrderStatus.OUT_FOR_DELIVERY}>Out for Delivery</option>
          <option value={OrderStatus.DELIVERY_SCHEDULED}>Delivery Scheduled</option>
          <option value={OrderStatus.DELIVERED}>Delivered</option>
          <option value={OrderStatus.CANCELLED}>Cancelled</option>
        </select>
      </div>

      {/* Orders List */}
      {orders.length === 0 ? (
        <div className="text-center py-12">
          <Package className="h-16 w-16 text-gray-300 mx-auto mb-4" />
          <h2 className="text-xl font-semibold text-gray-900 mb-2">No orders found</h2>
          <p className="text-gray-600">
            {statusFilter === 'ALL' 
              ? "You haven't received any orders yet."
              : `No orders with status "${statusConfig[statusFilter]?.label || statusFilter}".`}
          </p>
        </div>
      ) : (
        <div>
          <p className="text-sm text-gray-500 mb-4">{orders.length} order(s) found</p>
          {orders.map((order) => (
            <OrderCard key={order.id} order={order} onStatusUpdate={() => refetch()} />
          ))}
        </div>
      )}
    </div>
  );
}

export default VendorOrdersPage;
