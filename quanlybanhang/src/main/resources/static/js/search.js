document.addEventListener('DOMContentLoaded', function () {

    const searchInput = document.getElementById('search-input-main');
    const searchResultsBox = document.getElementById('search-results-main');
    const searchForm = document.getElementById('search-form');




    if (searchInput && searchResultsBox && searchForm && searchInput.closest('body')) {
        console.log('[DEBUG SearchJS] Các element tìm kiếm chính đã được tìm thấy. Gắn event listeners...');
        let debounceSearchTimeout;

        searchInput.addEventListener('input', function () {
            console.log('[DEBUG SearchJS] Event: input - Giá trị:', this.value);
            const query = this.value.trim();
            clearTimeout(debounceSearchTimeout);
            if (query.length > 0) {
                searchResultsBox.innerHTML = '<div class="search-result-item" style="color: #888; text-align:center;">Đang tải gợi ý...</div>';
                searchResultsBox.style.display = 'block';
                console.log('[DEBUG SearchJS] Event: input - Sẽ gọi fetchTagSuggestions cho query:', query);
                debounceSearchTimeout = setTimeout(() => fetchTagSuggestions(query), 300);
            } else {
                console.log('[DEBUG SearchJS] Event: input - Query rỗng, ẩn box gợi ý.');
                searchResultsBox.style.display = 'none';
                searchResultsBox.innerHTML = '';
            }
        });

        searchInput.addEventListener('keydown', function (event) {
            if (event.key === 'Enter') {
                console.log('[DEBUG SearchJS] Event: keydown - Phím Enter được nhấn.');
                event.preventDefault();
                if (searchInput.value.trim() !== '') {
                    console.log('[DEBUG SearchJS] Event: keydown - Submit form với tag:', searchInput.value);
                    searchForm.submit();
                } else {
                    console.log('[DEBUG SearchJS] Event: keydown - Ô input rỗng, không submit.');
                }
            }
        });

        document.addEventListener('click', function (e) {
            if (e.target !== searchInput && !searchResultsBox.contains(e.target) && !searchInput.contains(e.target)) {
                if (searchResultsBox.style.display !== 'none') {
                    console.log('[DEBUG SearchJS] Event: click ngoài - Ẩn box gợi ý.');
                    searchResultsBox.style.display = 'none';
                }
            }
        });

        searchInput.addEventListener('focus', function () {
            const query = this.value.trim();
            console.log('[DEBUG SearchJS] Event: focus - Query hiện tại:', query);
            if (query.length > 0) {
                console.log('[DEBUG SearchJS] Event: focus - Gọi fetchTagSuggestions cho query:', query);
                fetchTagSuggestions(query);
            } else if (searchResultsBox.children.length > 0) { // Nếu box có item nhưng query rỗng (ví dụ user xóa hết)
                console.log('[DEBUG SearchJS] Event: focus - Query rỗng nhưng box có children, ẩn box.');
                searchResultsBox.style.display = 'none';
            }
        });

        function fetchTagSuggestions(query) {
            console.log('[DEBUG SearchJS] fetchTagSuggestions - Đang fetch cho query:', query);
            fetch(`/api/products/tags/suggest?q=${encodeURIComponent(query)}`)
                .then(response => {
                    console.log('[DEBUG SearchJS] fetchTagSuggestions - Nhận response từ API, status:', response.status);
                    if (response.status === 204) return [];
                    if (!response.ok) {
                        console.error('[DEBUG SearchJS] fetchTagSuggestions - Lỗi HTTP:', response.statusText);
                        throw new Error(`HTTP error! status: ${response.status}`);
                    }
                    return response.json();
                })
                .then(tags => {
                    console.log('[DEBUG SearchJS] fetchTagSuggestions - Nhận được tags từ API:', tags);
                    displayTagSuggestions(tags, query);
                })
                .catch(error => {
                    console.error('[DEBUG SearchJS] fetchTagSuggestions - Lỗi khi fetch:', error);
                    if (searchResultsBox) {
                        searchResultsBox.innerHTML = '<div class="search-result-item" style="color: red;">Lỗi tải gợi ý.</div>';
                        searchResultsBox.style.display = 'block';
                    }
                });
        }

        function displayTagSuggestions(tags, originalQuery) {
            if (!searchResultsBox) {
                console.error('[DEBUG SearchJS] displayTagSuggestions - LỖI: searchResultsBox là null!');
                return;
            }
            console.log('[DEBUG SearchJS] displayTagSuggestions - Hiển thị tags:', tags, 'cho query:', originalQuery);
            searchResultsBox.innerHTML = '';

            if (tags && tags.length > 0) {
                console.log('[DEBUG SearchJS] displayTagSuggestions - Có tags để hiển thị.');
                tags.forEach(tag => {
                    const item = document.createElement('div');
                    item.classList.add('search-result-item');
                    // Highlight logic (giữ nguyên)
                    const lowerCaseTag = tag.toLowerCase();
                    const lowerCaseQuery = originalQuery.toLowerCase();
                    let startIndex = lowerCaseTag.indexOf(lowerCaseQuery);
                    let highlightedText = '';
                    if (startIndex !== -1) {
                        let currentIndex = 0;
                        while (startIndex !== -1) {
                            highlightedText += tag.substring(currentIndex, startIndex);
                            highlightedText += '<strong>' + tag.substring(startIndex, startIndex + originalQuery.length) + '</strong>';
                            currentIndex = startIndex + originalQuery.length;
                            startIndex = lowerCaseTag.indexOf(lowerCaseQuery, currentIndex);
                        }
                        highlightedText += tag.substring(currentIndex);
                        item.innerHTML = highlightedText;
                    } else {
                        item.textContent = tag;
                    }
                    item.addEventListener('click', function () {
                        console.log('[DEBUG SearchJS] displayTagSuggestions - Item gợi ý được click:', tag);
                        if (searchInput) searchInput.value = tag;
                        searchResultsBox.style.display = 'none';
                        if (searchForm) searchForm.submit();
                    });
                    searchResultsBox.appendChild(item);
                });
                searchResultsBox.style.display = 'block';
                console.log('[DEBUG SearchJS] displayTagSuggestions - Đã hiển thị searchResultsBox.');
            } else {
                console.log('[DEBUG SearchJS] displayTagSuggestions - Không có tags để hiển thị hoặc query rỗng.');
                if (originalQuery && originalQuery.length > 0) {
                    const noResultItem = document.createElement('div');
                    noResultItem.classList.add('search-result-item');
                    noResultItem.style.color = '#777';
                    noResultItem.textContent = `Không có gợi ý cho "${originalQuery}"`;
                    searchResultsBox.appendChild(noResultItem);
                    searchResultsBox.style.display = 'block';
                    console.log('[DEBUG SearchJS] displayTagSuggestions - Đã hiển thị "Không có gợi ý".');
                } else {
                    searchResultsBox.style.display = 'none';
                    console.log('[DEBUG SearchJS] displayTagSuggestions - Query rỗng, ẩn box.');
                }
            }
        }
    } else {
        console.error('[DEBUG SearchJS] LỖI: Một hoặc nhiều element tìm kiếm chính (searchInput, searchResultsBox, searchForm) không được tìm thấy trong DOM!');
    }


});