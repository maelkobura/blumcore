use std::process::Command;
use std::env;
use std::os::windows::process::CommandExt;

const JRE: &str = "runtime/bin/java.exe";
const LIBS: &str = "libs";
const APP: &str = "libs/app.jar";
const MAIN: &str = "blum.core.system.BlumBoot";

fn main() {

    let args: Vec<String> = env::args().skip(1).collect();

    let exe_dir = env::current_exe()
        .expect("Failed to localize BlumCore launcher")
        .parent()
        .unwrap()
        .to_path_buf();

    let root_dir = exe_dir.parent().unwrap().to_path_buf();

    let java_bin = root_dir.join(JRE);

    let app_jar = root_dir.join(APP);
    let libs_dir = root_dir.join(LIBS).join("*");

    let classpath = format!("{};{}", app_jar.display(), libs_dir.display());

    let mut cmd = Command::new(java_bin);
    cmd.creation_flags(0x08000000); //no console

    cmd.arg("-Xmx2G")
        .arg("-Xms512M")
        .arg("-cp")
        .arg(classpath)
        .arg(MAIN)
        .args(&args);

    let status = cmd.status().expect("Failed to load Blum application");

    std::process::exit(status.code().unwrap_or(1));
}