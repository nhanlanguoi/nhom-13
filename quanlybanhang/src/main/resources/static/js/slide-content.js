document.addEventListener("DOMContentLoaded", function () {
    const images = document.querySelectorAll('.slide-track img');
    const slideContents = document.querySelectorAll('.slide-content-container .slide-content');
    const prevBtn = document.querySelector('.previous-image');
    const nextBtn = document.querySelector('.next-image');
    let currentIndex = 0;

    function showSlide(index) {
        images.forEach((img, i) => {
            img.classList.toggle('active', i === index);
        });
        slideContents.forEach((content, i) => {
            content.classList.toggle('active', i === index);
        });
    }



    prevBtn.addEventListener('click', function () {
        currentIndex = (currentIndex - 1 + images.length) % images.length;
        showSlide(currentIndex);
    });

    nextBtn.addEventListener('click', function () {
        currentIndex = (currentIndex + 1) % images.length;
        showSlide(currentIndex);
    });

    showSlide(currentIndex);
});
