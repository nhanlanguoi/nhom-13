document.addEventListener('DOMContentLoaded', function () {
    const images = Array.from(document.querySelectorAll('.slide-track-inner img'));
    const contents = Array.from(document.querySelectorAll('.slide-content-container .slide-content'));
    const trackInner = document.querySelector('.slide-track-inner');

    if (images[0]) images[0].classList.add('active');
    if (contents[0]) contents[0].classList.add('active');

    // Hàm chuyển ảnh theo index
    function showImage(index) {
        images.forEach(img => img.classList.remove('active'));
        if (images[index]) images[index].classList.add('active');
        // Nếu dùng transform để trượt ảnh
        if (trackInner) {
            trackInner.style.transform = `translateX(-${index * 100}%)`;
        }
    }

    // Click vào content: chuyển ảnh tương ứng như hiệu ứng arrow
    contents.forEach((content, i) => {
        content.addEventListener('click', function () {
            const currentActiveIndex = contents.findIndex(c => c.classList.contains('active'));
            if (currentActiveIndex === i) return; // Đã active rồi

            // Tính số lần chuyển (có thể dùng cho hiệu ứng chuyển dần nếu muốn)
            const diff = Math.abs(i - currentActiveIndex);

            // Đổi active cho content
            contents.forEach(c => c.classList.remove('active'));
            content.classList.add('active');

            // Đổi ảnh tương ứng
            showImage(i);
        });
    });

    images.forEach((img, i) => {
        img.addEventListener('click', function () {
            images.forEach(im => im.classList.remove('active'));
            contents.forEach(c => c.classList.remove('active'));
            img.classList.add('active');
            if (contents[i]) contents[i].classList.add('active');
            showImage(i);
        });
    });
});

