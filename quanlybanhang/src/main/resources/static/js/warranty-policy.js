document.addEventListener('DOMContentLoaded', function () {
  const optionItems = document.querySelectorAll('.warranty-options-item');
  const childItems = document.querySelectorAll('.warranty-options-child-item');

  function updateChildItems() {
    document.querySelectorAll('.warranty-options-child-item').forEach(child => {
      child.style.display = 'none';
    });

    // Hiện child-item của option-item có selected
    optionItems.forEach(item => {
      if (item.classList.contains('selected')) {
        let next = item.nextElementSibling;
        if (next && next.classList.contains('warranty-options-child-item')){
            next.classList.add('selected');
        }
        while (next && next.classList.contains('warranty-options-child-item')) {
          next.style.display = 'block';
          next = next.nextElementSibling;
        }
      }
    });
  }
  //Thêm selected cho option-item khi click
  optionItems.forEach(item => {
    item.addEventListener('click', function () {
      optionItems.forEach(i => i.classList.remove('selected'));
      childItems.forEach(i => i.classList.remove('selected'));
      this.classList.add('selected');
      updateChildItems();
    });
  });
  
  // Thêm selected cho child-item khi click
  childItems.forEach(child => {
    child.addEventListener('click', function () {
      childItems.forEach(c => c.classList.remove('selected'));
      this.classList.add('selected');
    });
  });

  // Khởi tạo trạng thái ban đầu
  updateChildItems();

  
});


document.addEventListener('DOMContentLoaded', function () {
  const optionItems = document.querySelectorAll('.warranty-options-item');
  const childItems = document.querySelectorAll('.warranty-options-child-item');

  const a1 = [];
optionItems.forEach(item => {
  const next = item.nextElementSibling;
  if (!(next && next.classList.contains('warranty-options-child-item'))) {
    a1.push(item);
  }
});
childItems.forEach(child => a1.push(child));

// Tạo mảng a2 chứa các warranty-content
const a2 = Array.from(document.querySelectorAll('.warranty-content'));

// Thêm 'active' cho phần tử trong a2 tương ứng với phần tử selected trong a1
function updateActiveContent() {
  a2.forEach(el => el.classList.remove('active'));
  a1.forEach((el, idx) => {
    if (el.classList.contains('selected')) {
      if (a2[idx]) a2[idx].classList.add('active');
    }
  });
}

// Gọi updateActiveContent mỗi khi updateChildItems hoặc khi chọn child
optionItems.forEach(item => {
  item.addEventListener('click', updateActiveContent);
});
childItems.forEach(child => {
  child.addEventListener('click', updateActiveContent);
});

// Khởi tạo trạng thái ban đầu cho a2
updateActiveContent();


});