document.addEventListener("DOMContentLoaded", function () {
    const trackInner = document.querySelector('.slide-track-inner');
    const images = document.querySelectorAll('.slide-track img');
    const prevBtn = document.querySelector('.previous-image');
    const nextBtn = document.querySelector('.next-image');
    let currentIndex = 0;

    function showImage(index) {
        trackInner.style.transform = `translateX(-${index * 100}%)`;
    }

    prevBtn.addEventListener('click', function () {
        currentIndex = (currentIndex - 1 + images.length) % images.length;
        showImage(currentIndex);
    });

    nextBtn.addEventListener('click', function () {
        currentIndex = (currentIndex + 1) % images.length;
        showImage(currentIndex);
    });

    showImage(currentIndex);
});