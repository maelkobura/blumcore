fn main() {
    let mut res = winres::WindowsResource::new();
    res.set_icon("icon.ico"); // chemin vers ton .ico
        res.set("FileDescription", "Blum Core Background Service"); // Nom affiché
        res.set("ProductName", "Blum Core");
    res.compile().unwrap();
}