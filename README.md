<div align="center">

# 🍓 FRUIT POP ADVENTURE

![platform](https://img.shields.io/badge/platform-Android-brightgreen)
![compileSdk](https://img.shields.io/badge/compileSdk-32-brightgreen)
![Firebase](https://img.shields.io/badge/Firebase-Firestore-orange)
![Admin](https://img.shields.io/badge/Admin-React-blue)

**Fruit Pop Adventure** là trò chơi bắn và kết hợp các loại trái cây trên nền tảng Android, được phát triển và tùy chỉnh từ dự án mã nguồn mở Animals Pop.

</div>

---

## 📌 Giới thiệu

Fruit Pop Adventure là trò chơi Android thuộc thể loại Bubble Shooter.

Người chơi điều khiển và bắn các loại trái cây vào khu vực chơi. Khi có từ 3 đối tượng cùng loại được kết nối với nhau, chúng sẽ được loại bỏ khỏi màn chơi.

Mục tiêu của người chơi là hoàn thành yêu cầu của từng màn với số lượt chơi được giới hạn.

Dự án bao gồm:

* Ứng dụng game Fruit Pop Adventure trên Android.
* Website quản trị được xây dựng bằng React.
* Firebase Firestore dùng để lưu trữ và đồng bộ dữ liệu giữa website quản trị và ứng dụng Android.

---

## 🎮 Gameplay

Luồng chơi cơ bản:

1. Người chơi chọn màn chơi.
2. Hệ thống tải dữ liệu của màn.
3. Người chơi ngắm hướng và bắn trái cây.
4. Khi có từ 3 trái cây cùng loại kết nối với nhau, chúng sẽ được loại bỏ.
5. Các đối tượng không còn liên kết với vùng phía trên sẽ rơi khỏi màn chơi.
6. Hệ thống cập nhật tiến trình và số lượt chơi còn lại.
7. Người chơi hoàn thành mục tiêu để vượt qua màn chơi.

Dự án hiện có hệ thống dữ liệu gồm **15 màn chơi**.

---

## ⚙️ Chức năng chính

### Ứng dụng Android

* Chơi game theo cơ chế Bubble Shooter.
* Chọn và chơi các màn đã được thiết kế.
* Hiển thị mục tiêu của từng màn chơi.
* Quản lý số lượt chơi của người chơi.
* Lưu và đọc dữ liệu cần thiết của trò chơi.
* Nhận dữ liệu cấu hình từ Firebase Firestore.
* Đồng bộ thay đổi dữ liệu từ hệ thống quản trị.

### Website quản trị

* Đăng nhập quản trị viên.
* Theo dõi dữ liệu người chơi.
* Quản lý dữ liệu màn chơi.
* Điều chỉnh số lượt chơi của người chơi.
* Gửi yêu cầu thay đổi dữ liệu thông qua Firebase Firestore.
* Theo dõi kết quả đồng bộ giữa website và ứng dụng Android.

---

## 🔥 Firebase Firestore

Firebase Firestore được sử dụng làm thành phần trung gian để lưu trữ và đồng bộ dữ liệu giữa ứng dụng Android và website quản trị.

Luồng xử lý tổng quát:

```text
Website Admin
      ↓
Firebase Firestore
      ↓
Ứng dụng Android
      ↓
Firebase Firestore
```

Website quản trị có thể cập nhật dữ liệu lên Firestore. Ứng dụng Android đọc dữ liệu tương ứng, thực hiện cập nhật và gửi kết quả xử lý trở lại Firestore.

---

## 🛠️ Công nghệ sử dụng

### Android

* Java
* Android SDK
* Gradle
* Firebase Firestore
* Firebase Android SDK

### Website Admin

* React
* Vite
* JavaScript
* Firebase Web SDK
* Firebase Firestore

### Công cụ

* Android Studio
* Visual Studio Code
* Git
* GitHub
* Firebase Console

---

## 🚀 Cài đặt và chạy dự án

### Android

Clone repository:

```bash
git clone https://github.com/Tomoe037/fruitpop.git
```

Mở thư mục dự án bằng **Android Studio**.

Đồng bộ Gradle và chờ Android Studio tải các dependency cần thiết.

Sau đó có thể:

* Chạy ứng dụng bằng Android Emulator.
* Hoặc build file APK để cài đặt trực tiếp trên thiết bị Android.

---

## 🌐 Chạy Website Admin

Di chuyển vào thư mục website quản trị và cài đặt dependency:

```bash
npm install
```

Khởi chạy môi trường phát triển:

```bash
npm run dev
```

Website quản trị sử dụng Firebase Firestore để trao đổi dữ liệu với ứng dụng Android.

---

## 📂 Mã nguồn

Mã nguồn dự án Fruit Pop Adventure:

https://github.com/Tomoe037/fruitpop

Mã nguồn gốc được tham khảo và phát triển:

https://github.com/natygames/animals-pop

---

## 🤝 Credit

Fruit Pop Adventure được phát triển và tùy chỉnh dựa trên mã nguồn mở:

* Animals Pop: https://github.com/natygames/animals-pop
* Natty Engine: https://github.com/nativegamestudio/natty-engine
* Graph-in-Bubble-Shooter: https://github.com/nativegamestudio/Graph-in-Bubble-Shooter

Giao diện, hình ảnh, nội dung trò chơi và các chức năng quản trị đã được chỉnh sửa và phát triển để phù hợp với dự án **Fruit Pop Adventure**.

---

## 📄 License

Dự án gốc Animals Pop được phát hành theo giấy phép MIT.

Khi sử dụng hoặc phát triển tiếp mã nguồn, cần tuân thủ các điều khoản giấy phép của dự án mã nguồn gốc.
