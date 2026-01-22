import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { ShoppingBag, CreditCard, ArrowLeft, Lock, CheckCircle, Truck, Banknote } from 'lucide-react';
import { useCartStore } from '../store/cartStore';
import { useAuthStore } from '../store/authStore';
import { ordersApi } from '../api/orders';
import { paymentsApi } from '../api/payments';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { ShippingAddressRequest, PaymentMethod } from '../types';
import toast from 'react-hot-toast';

export function CheckoutPage() {
  const navigate = useNavigate();
  const { items, getTotalPrice, clearCart } = useCartStore();
  const { isAuthenticated } = useAuthStore();
  
  const [step, setStep] = useState<'shipping' | 'payment' | 'confirmation'>('shipping');
  const [orderId, setOrderId] = useState<string | null>(null);
  
  const [shippingAddress, setShippingAddress] = useState<ShippingAddressRequest>({
    fullName: '',
    addressLine1: '',
    addressLine2: '',
    city: '',
    state: '',
    postalCode: '',
    country: '',
    phoneNumber: '',
  });

  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>(PaymentMethod.CREDIT_CARD);
  const [cardDetails, setCardDetails] = useState({
    cardNumber: '',
    expiryMonth: '',
    expiryYear: '',
    cvv: '',
    cardHolderName: '',
  });

  const totalPrice = getTotalPrice();

  const createOrderMutation = useMutation({
    mutationFn: ordersApi.create,
    onSuccess: (order) => {
      setOrderId(order.id);
      setStep('payment');
    },
    onError: (err: unknown) => {
      if (err && typeof err === 'object' && 'response' in err) {
        const axiosError = err as { response?: { data?: { message?: string } } };
        toast.error(axiosError.response?.data?.message || 'Failed to create order');
      } else {
        toast.error('Failed to create order');
      }
    },
  });

  const initiatePaymentMutation = useMutation({
    mutationFn: paymentsApi.initiatePayment,
    onSuccess: async (payment) => {
      // For Cash on Delivery, skip the process step
      if (paymentMethod === PaymentMethod.CASH_ON_DELIVERY) {
        clearCart();
        setStep('confirmation');
        toast.success('Order placed successfully! Pay on delivery.');
        return;
      }
      
      // For card payments, process the payment
      try {
        await paymentsApi.processPayment({
          paymentId: payment.id,
          simulateSuccess: true, // In a real app, this would be based on actual card processing
        });
        clearCart();
        setStep('confirmation');
        toast.success('Payment successful!');
      } catch (err) {
        toast.error('Payment processing failed. Please try again.');
      }
    },
    onError: (err: unknown) => {
      if (err && typeof err === 'object' && 'response' in err) {
        const axiosError = err as { response?: { data?: { message?: string } } };
        toast.error(axiosError.response?.data?.message || 'Payment failed');
      } else {
        toast.error('Payment failed');
      }
    },
  });

  if (!isAuthenticated) {
    return (
      <div className="max-w-2xl mx-auto px-4 py-12 text-center">
        <Lock className="h-16 w-16 text-gray-300 mx-auto mb-4" />
        <h2 className="text-2xl font-bold text-gray-900 mb-2">Sign In Required</h2>
        <p className="text-gray-600 mb-4">Please sign in to proceed with checkout.</p>
        <Link to="/login" className="btn btn-primary">
          Sign In
        </Link>
      </div>
    );
  }

  if (items.length === 0 && step !== 'confirmation') {
    return (
      <div className="max-w-2xl mx-auto px-4 py-12 text-center">
        <ShoppingBag className="h-16 w-16 text-gray-300 mx-auto mb-4" />
        <h2 className="text-2xl font-bold text-gray-900 mb-2">Your Cart is Empty</h2>
        <p className="text-gray-600 mb-4">Add some products before checkout.</p>
        <Link to="/products" className="btn btn-primary">
          Browse Products
        </Link>
      </div>
    );
  }

  const handleShippingSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    createOrderMutation.mutate({
      items: items.map((item) => ({
        productId: item.product.id,
        quantity: item.quantity,
      })),
      shippingAddress,
    });
  };

  const handlePaymentSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!orderId) return;
    
    initiatePaymentMutation.mutate({
      orderId,
      paymentMethod: paymentMethod,
    });
  };

  const handleAddressChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setShippingAddress({ ...shippingAddress, [e.target.name]: e.target.value });
  };

  const handleCardChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setCardDetails({ ...cardDetails, [e.target.name]: e.target.value });
  };

  // Confirmation Step
  if (step === 'confirmation') {
    return (
      <div className="max-w-2xl mx-auto px-4 py-12 text-center">
        <div className="bg-green-100 rounded-full w-20 h-20 flex items-center justify-center mx-auto mb-6">
          <CheckCircle className="h-10 w-10 text-green-600" />
        </div>
        <h1 className="text-3xl font-bold text-gray-900 mb-4">Order Confirmed!</h1>
        <p className="text-gray-600 mb-2">Thank you for your purchase.</p>
        <p className="text-gray-600 mb-8">
          Order ID: <span className="font-mono font-medium">{orderId}</span>
        </p>
        <div className="flex flex-col sm:flex-row gap-4 justify-center">
          <Link to="/orders" className="btn btn-primary">
            View Orders
          </Link>
          <Link to="/products" className="btn btn-secondary">
            Continue Shopping
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto px-4 py-8">
      {/* Header */}
      <div className="mb-8">
        <button
          onClick={() => navigate(-1)}
          className="flex items-center text-gray-600 hover:text-gray-900 mb-4"
        >
          <ArrowLeft className="h-4 w-4 mr-2" />
          Back
        </button>
        <h1 className="text-3xl font-bold text-gray-900">Checkout</h1>
      </div>

      {/* Progress Steps */}
      <div className="flex items-center justify-center mb-8">
        <div className="flex items-center">
          <div className={`w-10 h-10 rounded-full flex items-center justify-center ${
            step === 'shipping' ? 'bg-primary-600 text-white' : 'bg-green-500 text-white'
          }`}>
            1
          </div>
          <span className="ml-2 font-medium">Shipping</span>
        </div>
        <div className={`w-16 h-1 mx-4 ${step === 'payment' ? 'bg-primary-600' : 'bg-gray-200'}`} />
        <div className="flex items-center">
          <div className={`w-10 h-10 rounded-full flex items-center justify-center ${
            step === 'payment' ? 'bg-primary-600 text-white' : 'bg-gray-200 text-gray-500'
          }`}>
            2
          </div>
          <span className="ml-2 font-medium">Payment</span>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Form */}
        <div className="lg:col-span-2">
          {step === 'shipping' && (
            <form onSubmit={handleShippingSubmit} className="card p-6 space-y-6">
              <h2 className="text-xl font-semibold text-gray-900">Shipping Address</h2>
              
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label htmlFor="fullName" className="label">Full Name *</label>
                  <input
                    id="fullName"
                    name="fullName"
                    type="text"
                    required
                    value={shippingAddress.fullName}
                    onChange={handleAddressChange}
                    className="input"
                    placeholder="John Doe"
                  />
                </div>
                <div>
                  <label htmlFor="phoneNumber" className="label">Phone Number *</label>
                  <input
                    id="phoneNumber"
                    name="phoneNumber"
                    type="tel"
                    required
                    value={shippingAddress.phoneNumber}
                    onChange={handleAddressChange}
                    className="input"
                    placeholder="+1 234 567 8900"
                  />
                </div>
              </div>

              <div>
                <label htmlFor="addressLine1" className="label">Address Line 1 *</label>
                <input
                  id="addressLine1"
                  name="addressLine1"
                  type="text"
                  required
                  value={shippingAddress.addressLine1}
                  onChange={handleAddressChange}
                  className="input"
                  placeholder="123 Main Street"
                />
              </div>

              <div>
                <label htmlFor="addressLine2" className="label">Address Line 2 (Optional)</label>
                <input
                  id="addressLine2"
                  name="addressLine2"
                  type="text"
                  value={shippingAddress.addressLine2 || ''}
                  onChange={handleAddressChange}
                  className="input"
                  placeholder="Apt, Suite, Building (optional)"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label htmlFor="city" className="label">City *</label>
                  <input
                    id="city"
                    name="city"
                    type="text"
                    required
                    value={shippingAddress.city}
                    onChange={handleAddressChange}
                    className="input"
                    placeholder="New York"
                  />
                </div>
                <div>
                  <label htmlFor="state" className="label">State *</label>
                  <input
                    id="state"
                    name="state"
                    type="text"
                    required
                    value={shippingAddress.state}
                    onChange={handleAddressChange}
                    className="input"
                    placeholder="NY"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label htmlFor="postalCode" className="label">Postal Code *</label>
                  <input
                    id="postalCode"
                    name="postalCode"
                    type="text"
                    required
                    value={shippingAddress.postalCode}
                    onChange={handleAddressChange}
                    className="input"
                    placeholder="10001"
                  />
                </div>
                <div>
                  <label htmlFor="country" className="label">Country *</label>
                  <input
                    id="country"
                    name="country"
                    type="text"
                    required
                    value={shippingAddress.country}
                    onChange={handleAddressChange}
                    className="input"
                    placeholder="United States"
                  />
                </div>
              </div>

              <button
                type="submit"
                disabled={createOrderMutation.isPending}
                className="btn btn-primary w-full py-3"
              >
                {createOrderMutation.isPending ? (
                  <LoadingSpinner size="sm" className="text-white" />
                ) : (
                  'Continue to Payment'
                )}
              </button>
            </form>
          )}

          {step === 'payment' && (
            <form onSubmit={handlePaymentSubmit} className="card p-6 space-y-6">
              <h2 className="text-xl font-semibold text-gray-900">Payment Method</h2>

              {/* Payment Method Selection */}
              <div className="space-y-3">
                <label
                  className={`flex items-center space-x-4 p-4 rounded-lg border-2 cursor-pointer transition-colors ${
                    paymentMethod === PaymentMethod.CREDIT_CARD
                      ? 'border-primary-500 bg-primary-50'
                      : 'border-gray-200 hover:border-gray-300'
                  }`}
                >
                  <input
                    type="radio"
                    name="paymentMethod"
                    value={PaymentMethod.CREDIT_CARD}
                    checked={paymentMethod === PaymentMethod.CREDIT_CARD}
                    onChange={(e) => setPaymentMethod(e.target.value as PaymentMethod)}
                    className="h-4 w-4 text-primary-600"
                  />
                  <CreditCard className="h-6 w-6 text-gray-500" />
                  <div className="flex-1">
                    <p className="font-medium">Credit / Debit Card</p>
                    <p className="text-sm text-gray-500">Pay securely with your card</p>
                  </div>
                </label>

                <label
                  className={`flex items-center space-x-4 p-4 rounded-lg border-2 cursor-pointer transition-colors ${
                    paymentMethod === PaymentMethod.CASH_ON_DELIVERY
                      ? 'border-primary-500 bg-primary-50'
                      : 'border-gray-200 hover:border-gray-300'
                  }`}
                >
                  <input
                    type="radio"
                    name="paymentMethod"
                    value={PaymentMethod.CASH_ON_DELIVERY}
                    checked={paymentMethod === PaymentMethod.CASH_ON_DELIVERY}
                    onChange={(e) => setPaymentMethod(e.target.value as PaymentMethod)}
                    className="h-4 w-4 text-primary-600"
                  />
                  <Banknote className="h-6 w-6 text-gray-500" />
                  <div className="flex-1">
                    <p className="font-medium">Cash on Delivery</p>
                    <p className="text-sm text-gray-500">Pay when you receive your order</p>
                  </div>
                </label>
              </div>

              {/* Card Details - Only show if card payment selected */}
              {paymentMethod === PaymentMethod.CREDIT_CARD && (
                <div className="space-y-4 pt-4 border-t">
                  <h3 className="font-medium text-gray-900">Card Details</h3>
                  
                  <div>
                    <label htmlFor="cardHolderName" className="label">Cardholder Name</label>
                    <input
                      id="cardHolderName"
                      name="cardHolderName"
                      type="text"
                      required
                      value={cardDetails.cardHolderName}
                      onChange={handleCardChange}
                      className="input"
                      placeholder="John Doe"
                    />
                  </div>

                  <div>
                    <label htmlFor="cardNumber" className="label">Card Number</label>
                    <input
                      id="cardNumber"
                      name="cardNumber"
                      type="text"
                      required
                      value={cardDetails.cardNumber}
                      onChange={handleCardChange}
                      className="input"
                      placeholder="4242 4242 4242 4242"
                      maxLength={19}
                    />
                  </div>

                  <div className="grid grid-cols-3 gap-4">
                    <div>
                      <label htmlFor="expiryMonth" className="label">Month</label>
                      <input
                        id="expiryMonth"
                        name="expiryMonth"
                        type="text"
                        required
                        value={cardDetails.expiryMonth}
                        onChange={handleCardChange}
                        className="input"
                        placeholder="MM"
                        maxLength={2}
                      />
                    </div>
                    <div>
                      <label htmlFor="expiryYear" className="label">Year</label>
                      <input
                        id="expiryYear"
                        name="expiryYear"
                        type="text"
                        required
                        value={cardDetails.expiryYear}
                        onChange={handleCardChange}
                        className="input"
                        placeholder="YY"
                        maxLength={2}
                      />
                    </div>
                    <div>
                      <label htmlFor="cvv" className="label">CVV</label>
                      <input
                        id="cvv"
                        name="cvv"
                        type="text"
                        required
                        value={cardDetails.cvv}
                        onChange={handleCardChange}
                        className="input"
                        placeholder="123"
                        maxLength={4}
                      />
                    </div>
                  </div>

                  <div className="flex items-center text-sm text-gray-500 bg-gray-50 p-3 rounded-lg">
                    <Lock className="h-4 w-4 mr-2" />
                    Your payment information is secure and encrypted
                  </div>
                </div>
              )}

              {/* Cash on Delivery Info */}
              {paymentMethod === PaymentMethod.CASH_ON_DELIVERY && (
                <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
                  <div className="flex items-start space-x-3">
                    <Truck className="h-5 w-5 text-yellow-600 mt-0.5" />
                    <div>
                      <p className="font-medium text-yellow-800">Cash on Delivery</p>
                      <p className="text-sm text-yellow-700 mt-1">
                        Please keep exact change ready. Our delivery partner will collect 
                        <strong> ${totalPrice.toFixed(2)}</strong> when delivering your order.
                      </p>
                    </div>
                  </div>
                </div>
              )}

              <button
                type="submit"
                disabled={initiatePaymentMutation.isPending}
                className="btn btn-primary w-full py-3"
              >
                {initiatePaymentMutation.isPending ? (
                  <LoadingSpinner size="sm" className="text-white" />
                ) : paymentMethod === PaymentMethod.CASH_ON_DELIVERY ? (
                  'Place Order'
                ) : (
                  `Pay $${totalPrice.toFixed(2)}`
                )}
              </button>
            </form>
          )}
        </div>

        {/* Order Summary */}
        <div className="lg:col-span-1">
          <div className="card p-6 sticky top-24">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">Order Summary</h2>
            
            <div className="space-y-4 mb-6">
              {items.map((item) => (
                <div key={item.product.id} className="flex items-center space-x-4">
                  <div className="w-16 h-16 bg-gray-100 rounded-lg overflow-hidden">
                    <img
                      src={item.product.images?.[0] || `https://placehold.co/64x64/e2e8f0/64748b?text=${encodeURIComponent(item.product.name.slice(0, 8))}`}
                      alt={item.product.name}
                      className="w-16 h-16 rounded-lg object-cover"
                      onError={(e) => {
                        e.currentTarget.src = `https://placehold.co/64x64/e2e8f0/64748b?text=${encodeURIComponent(item.product.name.slice(0, 8))}`;
                      }}
                    />
                  </div>
                  <div className="flex-1">
                    <p className="font-medium text-gray-900 text-sm truncate">
                      {item.product.name}
                    </p>
                    <p className="text-sm text-gray-500">Qty: {item.quantity}</p>
                  </div>
                  <p className="font-medium">
                    ${(item.product.price * item.quantity).toFixed(2)}
                  </p>
                </div>
              ))}
            </div>

            <div className="border-t pt-4 space-y-2">
              <div className="flex justify-between text-sm">
                <span className="text-gray-600">Subtotal</span>
                <span>${totalPrice.toFixed(2)}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-gray-600">Shipping</span>
                <span className="text-green-600">Free</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-gray-600">Tax</span>
                <span>${(totalPrice * 0.08).toFixed(2)}</span>
              </div>
              <div className="flex justify-between font-semibold text-lg pt-2 border-t">
                <span>Total</span>
                <span>${(totalPrice * 1.08).toFixed(2)}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
