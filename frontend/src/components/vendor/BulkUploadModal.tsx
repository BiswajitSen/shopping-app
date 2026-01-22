import { useState, useRef } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { X, Upload, FileText, Download, CheckCircle, XCircle, AlertCircle } from 'lucide-react';
import { productsApi, BulkUploadResponse } from '../../api/products';
import toast from 'react-hot-toast';

// Generate CSV template content
const CSV_TEMPLATE = `name,category,description,price,stock,images
"Example Product 1","Electronics","A great electronic product",99.99,50,"https://example.com/image1.jpg;https://example.com/image2.jpg"
"Example Product 2","Clothing","Comfortable cotton t-shirt",29.99,100,"https://example.com/tshirt.jpg"
"Example Product 3","Home & Garden","Beautiful indoor plant",19.99,25,""`;

const downloadCSVTemplate = () => {
  const blob = new Blob([CSV_TEMPLATE], { type: 'text/csv;charset=utf-8;' });
  const link = document.createElement('a');
  const url = URL.createObjectURL(blob);
  link.setAttribute('href', url);
  link.setAttribute('download', 'product_upload_template.csv');
  link.style.visibility = 'hidden';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
};

interface BulkUploadModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export function BulkUploadModal({ isOpen, onClose }: BulkUploadModalProps) {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [uploadResult, setUploadResult] = useState<BulkUploadResponse | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const queryClient = useQueryClient();

  const uploadMutation = useMutation({
    mutationFn: (file: File) => productsApi.bulkUpload(file),
    onSuccess: (result) => {
      setUploadResult(result);
      queryClient.invalidateQueries({ queryKey: ['vendorProducts'] });
      if (result.failureCount === 0) {
        toast.success(`Successfully uploaded ${result.successCount} products!`);
      } else {
        toast.success(`Uploaded ${result.successCount} products, ${result.failureCount} failed`);
      }
    },
    onError: (error: Error) => {
      toast.error(error.message || 'Failed to upload products');
    },
  });

  const handleFileSelect = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      if (!file.name.toLowerCase().endsWith('.csv')) {
        toast.error('Please select a CSV file');
        return;
      }
      setSelectedFile(file);
      setUploadResult(null);
    }
  };

  const handleUpload = () => {
    if (selectedFile) {
      uploadMutation.mutate(selectedFile);
    }
  };

  const handleClose = () => {
    setSelectedFile(null);
    setUploadResult(null);
    onClose();
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    
    const file = e.dataTransfer.files?.[0];
    if (file) {
      if (!file.name.toLowerCase().endsWith('.csv')) {
        toast.error('Please drop a CSV file');
        return;
      }
      setSelectedFile(file);
      setUploadResult(null);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black bg-opacity-50">
      <div className="bg-white rounded-xl shadow-xl max-w-2xl w-full max-h-[90vh] overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b">
          <div>
            <h2 className="text-xl font-bold text-gray-900">Bulk Upload Products</h2>
            <p className="text-sm text-gray-500 mt-1">Upload multiple products using a CSV file</p>
          </div>
          <button
            onClick={handleClose}
            className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <X className="h-5 w-5 text-gray-500" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6 overflow-y-auto max-h-[calc(90vh-200px)]">
          {/* CSV Format Info */}
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
            <h3 className="font-medium text-blue-800 mb-2 flex items-center">
              <FileText className="h-4 w-4 mr-2" />
              CSV Format
            </h3>
            <p className="text-sm text-blue-700 mb-2">
              Your CSV file should have the following columns:
            </p>
            <code className="text-xs bg-blue-100 px-2 py-1 rounded block overflow-x-auto">
              name,category,description,price,stock,images
            </code>
            <ul className="text-xs text-blue-600 mt-2 space-y-1">
              <li>• <strong>images:</strong> Multiple image URLs separated by semicolons (;)</li>
              <li>• <strong>description:</strong> Can be empty</li>
              <li>• Use quotes for values containing commas</li>
            </ul>
            <button
              onClick={downloadCSVTemplate}
              type="button"
              className="inline-flex items-center mt-3 text-sm text-blue-700 hover:text-blue-800 font-medium"
            >
              <Download className="h-4 w-4 mr-1" />
              Download Template
            </button>
          </div>

          {/* Upload Area */}
          {!uploadResult && (
            <div
              onDragOver={handleDragOver}
              onDrop={handleDrop}
              onClick={() => fileInputRef.current?.click()}
              className={`border-2 border-dashed rounded-xl p-8 text-center cursor-pointer transition-colors
                ${selectedFile 
                  ? 'border-green-300 bg-green-50' 
                  : 'border-gray-300 hover:border-primary-400 hover:bg-gray-50'
                }`}
            >
              <input
                ref={fileInputRef}
                type="file"
                accept=".csv"
                onChange={handleFileSelect}
                className="hidden"
              />
              
              {selectedFile ? (
                <div>
                  <CheckCircle className="h-12 w-12 text-green-500 mx-auto mb-3" />
                  <p className="font-medium text-gray-900">{selectedFile.name}</p>
                  <p className="text-sm text-gray-500 mt-1">
                    {(selectedFile.size / 1024).toFixed(1)} KB
                  </p>
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      setSelectedFile(null);
                    }}
                    className="text-sm text-red-600 hover:text-red-700 mt-2"
                  >
                    Remove file
                  </button>
                </div>
              ) : (
                <div>
                  <Upload className="h-12 w-12 text-gray-400 mx-auto mb-3" />
                  <p className="font-medium text-gray-700">Drop your CSV file here</p>
                  <p className="text-sm text-gray-500 mt-1">or click to browse</p>
                </div>
              )}
            </div>
          )}

          {/* Upload Result */}
          {uploadResult && (
            <div className="space-y-4">
              {/* Summary */}
              <div className="grid grid-cols-3 gap-4">
                <div className="bg-gray-50 rounded-lg p-4 text-center">
                  <p className="text-2xl font-bold text-gray-900">{uploadResult.totalRows}</p>
                  <p className="text-sm text-gray-500">Total Rows</p>
                </div>
                <div className="bg-green-50 rounded-lg p-4 text-center">
                  <p className="text-2xl font-bold text-green-600">{uploadResult.successCount}</p>
                  <p className="text-sm text-green-600">Successful</p>
                </div>
                <div className="bg-red-50 rounded-lg p-4 text-center">
                  <p className="text-2xl font-bold text-red-600">{uploadResult.failureCount}</p>
                  <p className="text-sm text-red-600">Failed</p>
                </div>
              </div>

              {/* Errors */}
              {uploadResult.errors.length > 0 && (
                <div className="bg-red-50 border border-red-200 rounded-lg p-4">
                  <h4 className="font-medium text-red-800 mb-2 flex items-center">
                    <AlertCircle className="h-4 w-4 mr-2" />
                    Errors ({uploadResult.errors.length})
                  </h4>
                  <div className="space-y-2 max-h-40 overflow-y-auto">
                    {uploadResult.errors.map((error, index) => (
                      <div key={index} className="text-sm bg-white rounded p-2">
                        <span className="font-medium text-red-700">Row {error.rowNumber}</span>
                        {error.productName && (
                          <span className="text-gray-600"> ({error.productName})</span>
                        )}
                        <span className="text-red-600">: {error.errorMessage}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Success List */}
              {uploadResult.successfulProducts.length > 0 && (
                <div className="bg-green-50 border border-green-200 rounded-lg p-4">
                  <h4 className="font-medium text-green-800 mb-2 flex items-center">
                    <CheckCircle className="h-4 w-4 mr-2" />
                    Successfully Added ({uploadResult.successfulProducts.length})
                  </h4>
                  <div className="space-y-1 max-h-40 overflow-y-auto">
                    {uploadResult.successfulProducts.map((product, index) => (
                      <div key={index} className="text-sm text-green-700">
                        • {product.name} - ${product.price}
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Upload More */}
              <button
                onClick={() => {
                  setSelectedFile(null);
                  setUploadResult(null);
                }}
                className="text-sm text-primary-600 hover:text-primary-700 font-medium"
              >
                Upload another file
              </button>
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="flex justify-end space-x-3 p-6 border-t bg-gray-50">
          <button onClick={handleClose} className="btn btn-secondary">
            {uploadResult ? 'Close' : 'Cancel'}
          </button>
          {!uploadResult && (
            <button
              onClick={handleUpload}
              disabled={!selectedFile || uploadMutation.isPending}
              className="btn btn-primary"
            >
              {uploadMutation.isPending ? (
                <>
                  <span className="animate-spin mr-2">⏳</span>
                  Uploading...
                </>
              ) : (
                <>
                  <Upload className="h-4 w-4 mr-2" />
                  Upload Products
                </>
              )}
            </button>
          )}
        </div>
      </div>
    </div>
  );
}

export default BulkUploadModal;
