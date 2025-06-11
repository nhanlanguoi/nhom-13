document.addEventListener("DOMContentLoaded", function () {
    const usernameInput = document.querySelector("#username");
    const passwordInput = document.querySelector("#password");

    usernameInput.addEventListener("focus", function () {
        usernameInput.placeholder = "";
    });
    usernameInput.addEventListener("blur", function () {
        usernameInput.placeholder = "Nhập tên đăng nhập của bạn";
    });

    passwordInput.addEventListener("focus", function () {
        passwordInput.placeholder = "";
    });
    passwordInput.addEventListener("blur", function () {
        passwordInput.placeholder = "Nhập mật khẩu của bạn";
    });
});



