const fs = require("fs"), path = require("path");
const bSrc = fs.existsSync("www/books.json") ? "www/books.json" : (fs.existsSync("books.json") ? "books.json" : null);
if (bSrc) {
  fs.mkdirSync("android/app/src/main/assets", { recursive: true });
  fs.copyFileSync(bSrc, "android/app/src/main/assets/books.json");
}
console.log("NATIVE_APP_READY");
