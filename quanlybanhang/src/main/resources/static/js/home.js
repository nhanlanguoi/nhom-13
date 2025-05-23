document.addEventListener('DOMContentLoaded', function () {
      
      const categoryButtons = document.querySelectorAll('.category-button');
      const productsGrid = document.getElementById('products-grid-main');
      const noCateProductsMessage = document.getElementById('no-products-message');
      const initialNoProductsContainer = document.getElementById('initial-no-products-container');
      const sectionTitle = document.querySelector('.product-display .section-title');

      if(categoryButtons.length > 0 && productsGrid && productsGrid.closest('body')) {
          categoryButtons.forEach(button => {
              button.addEventListener('click', function () {
                  const categoryName = this.dataset.category;
                  if (sectionTitle) sectionTitle.textContent = 'Sản phẩm - ' + categoryName;
                  if(initialNoProductsContainer && initialNoProductsContainer.closest('body')) {
                      initialNoProductsContainer.style.display = 'none';
                  }
                  fetchProductsByCategory(categoryName);
              });
          });
      }

      function fetchProductsByCategory(categoryName) {
          if (!productsGrid || !productsGrid.closest('body')) return;
          productsGrid.innerHTML = '<p style="text-align:center; padding: 20px;">Đang tải sản phẩm...</p>';
          if(noCateProductsMessage && noCateProductsMessage.closest('body')) noCateProductsMessage.style.display = 'none';

          fetch(`/api/products/category/${encodeURIComponent(categoryName)}`)
              .then(response => {
                  if (response.status === 204) return [];
                  if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
                  return response.json();
              })
              .then(products => renderProducts(products))
              .catch(error => {
                  console.error('Error fetching products by category:', error);
                  if (productsGrid && productsGrid.closest('body')) {
                    productsGrid.innerHTML = '<p style="text-align:center; color:red; padding: 20px;">Lỗi khi tải sản phẩm.</p>';
                  }
                  if(noCateProductsMessage && noCateProductsMessage.closest('body')) noCateProductsMessage.style.display = 'none';
              });
      }

      function renderProducts(products) {
          if (!productsGrid || !productsGrid.closest('body')) return;
          productsGrid.innerHTML = '';
          if (products && products.length > 0) {
              if(noCateProductsMessage && noCateProductsMessage.closest('body')) noCateProductsMessage.style.display = 'none';
              products.forEach(product => {
                  let imageUrl = '/images/placeholder.png';
                  let productPriceText = 'Liên hệ';
                  let variantIdForLink = '#';
                  let colorPriceIdForLink = '#';

                  if (product.variants && product.variants.length > 0) {
                      const firstVariant = product.variants[0];
                      imageUrl = firstVariant.image ? firstVariant.image : imageUrl;
                      variantIdForLink = firstVariant.id ? firstVariant.id : '#';
                      if (firstVariant.colorprices && firstVariant.colorprices.length > 0) {
                          const firstColorPrice = firstVariant.colorprices[0];
                          productPriceText = firstColorPrice.price ? formatPrice(firstColorPrice.price) + '₫' : 'Liên hệ';
                          colorPriceIdForLink = firstColorPrice.id ? String(firstColorPrice.id) : '#';
                      }
                  }
                  const productIdForLink = product.id ? product.id : 'unknown';
                  const productPageLink = `/product/${encodeURIComponent(productIdForLink)}/${encodeURIComponent(variantIdForLink)}?colorid=${encodeURIComponent(colorPriceIdForLink)}`;
                  const productCardHTML = `
                      <div class="tag-product">
                          <a href="${productPageLink}" class="product-link">
                              <img class="product-image" src="${imageUrl}" alt="Hình ảnh ${product.name || 'Sản phẩm'}" onerror="this.onerror=null;this.src='/images/placeholder.png';"/>
                              <div class="product-info">
                                  <p class="product-name">${product.name ? product.name : 'Tên sản phẩm'}</p>
                                  <p class="product-price">${productPriceText}</p>
                                  <p class="product-brand">Thương hiệu: ${product.brand ? product.brand : 'N/A'}</p>
                              </div>
                          </a>
                      </div>
                  `;
                  productsGrid.insertAdjacentHTML('beforeend', productCardHTML);
              });
          } else {
              if(noCateProductsMessage && noCateProductsMessage.closest('body')) noCateProductsMessage.style.display = 'block';
          }
      }

      function formatPrice(priceValue) {
          try {
              const priceString = String(priceValue);
              const number = parseFloat(priceString.replace(/[^0-9.-]+/g,""));
              return isNaN(number) ? priceString : number.toLocaleString('vi-VN');
          } catch (e) { return String(priceValue); }
      }
      

      const currentYearSpan = document.getElementById('current-year');
      if (currentYearSpan && currentYearSpan.closest('body')) {
        currentYearSpan.textContent = new Date().getFullYear();
      }




      const adSliderElement = document.getElementById('homeAdvertisementSlider');
          const prevAdButton = document.getElementById('homePrevAd');
          const nextAdButton = document.getElementById('homeNextAd');
          const noAdsMsgElement = document.getElementById('noHomeAdsMessage');

          let currentAdIndex = 0;
          let adImageUrls = [];

          async function fetchAndDisplayAdvertisements() {
              // Kiểm tra sự tồn tại của các element trước khi tiếp tục
              if (!adSliderElement || !prevAdButton || !nextAdButton || !noAdsMsgElement) {
                  // console.log("Một hoặc nhiều element quảng cáo không tìm thấy trên trang này.");
                  return; // Không thực hiện gì nếu element không tồn tại (ví dụ trên trang khác không có slider)
              }

              try {
                  const response = await fetch('/api/advertisements/current-images'); // API đã tạo trong AdvertisementController
                  if (!response.ok) {
                      console.error('Lỗi tải danh sách quảng cáo:', response.status);
                      showNoAdsMessage();
                      return;
                  }
                  adImageUrls = await response.json();

                  if (adImageUrls && adImageUrls.length > 0) {
                      noAdsMsgElement.style.display = 'none';
                      adSliderElement.style.display = 'flex'; // Hoặc 'block' tùy theo CSS của slider
                      renderAdvertisements();

                      // Hiển thị/ẩn nút dựa trên số lượng quảng cáo
                      if (adImageUrls.length > 1) {
                          prevAdButton.style.display = 'block';
                          nextAdButton.style.display = 'block';
                      } else {
                          prevAdButton.style.display = 'none';
                          nextAdButton.style.display = 'none';
                      }
                  } else {
                      showNoAdsMessage();
                  }
              } catch (error) {
                  console.error('Lỗi khi fetch quảng cáo:', error);
                  showNoAdsMessage();
              }
          }

          function showNoAdsMessage() {
              if (!adSliderElement || !noAdsMsgElement || !prevAdButton || !nextAdButton) return;
              adSliderElement.innerHTML = '';
              adSliderElement.style.display = 'none';
              prevAdButton.style.display = 'none';
              nextAdButton.style.display = 'none';
              noAdsMsgElement.style.display = 'block';
          }

          function renderAdvertisements() {
              if (!adSliderElement) return;
              adSliderElement.innerHTML = ''; // Xóa nội dung cũ
              adImageUrls.forEach(imageUrl => {
                  const slideDiv = document.createElement('div');
                  slideDiv.classList.add('advertisement-slide'); // Cần CSS cho class này

                  const imgElement = document.createElement('img');
                  imgElement.src = imageUrl;
                  imgElement.alt = "Quảng cáo";
                  // Bạn có thể thêm style trực tiếp ở đây hoặc tốt hơn là trong file CSS
                  imgElement.style.width = "100%"; // Ví dụ
                  imgElement.style.height = "auto"; // Ví dụ
                  imgElement.style.objectFit = "cover"; // Hoặc 'contain'

                  slideDiv.appendChild(imgElement);
                  adSliderElement.appendChild(slideDiv);
              });
              updateSliderView();
          }

          function updateSliderView() {
              if (adImageUrls.length > 0 && adSliderElement) {
                  // CSS cho .slider nên là display: flex; và overflow: hidden;
                  // CSS cho .advertisement-slide là min-width: 100%; transition: transform 0.5s ease-in-out;
                  adSliderElement.style.transform = `translateX(-${currentAdIndex * 100}%)`;
              }
          }

          if (nextAdButton) {
              nextAdButton.addEventListener('click', () => {
                  if (adImageUrls.length > 1) {
                      currentAdIndex = (currentAdIndex + 1) % adImageUrls.length;
                      updateSliderView();
                  }
              });
          }

          if (prevAdButton) {
              prevAdButton.addEventListener('click', () => {
                  if (adImageUrls.length > 1) {
                      currentAdIndex = (currentAdIndex - 1 + adImageUrls.length) % adImageUrls.length;
                      updateSliderView();
                  }
              });
          }


          fetchAndDisplayAdvertisements();

      

});