package blum.core.plugins;

import blum.api.plugins.PluginData;
import blum.core.util.FileUtil;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Getter
public class PluginClassLoader extends URLClassLoader {

    private File file;
    private List<ClassInfo> classes;

    public PluginClassLoader(File jar, ClassLoader parent) throws MalformedURLException {
        super(new URL[]{jar.toURI().toURL()}, parent);
        this.file = jar;
    }

    public PluginData getPluginData() throws IOException {

        InputStream in = getResourceAsStream("plugin.properties");
        if(in == null) {
            return new PluginData(FileUtil.stripExtension(file), "unknown", "no description", "unknown", "unknown", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }

        Properties prop = new Properties();
        prop.load(in);

        String name = prop.getProperty("name", FileUtil.stripExtension(file));
        String version = prop.getProperty("version", "unknown");
        String description = prop.getProperty("description", "no description");
        String author = prop.getProperty("author", "unknown");
        String website = prop.getProperty("website", "unknown");

        return new PluginData(name, version, description, author, website, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public void scan() {
        ScanResult scanResult = new ClassGraph()
                .addClassLoader(this)
                .enableClassInfo()
                .scan();

        this.classes = scanResult.getAllClasses();
    }

    public <T> List<Class<T>> getClassesWithSuperclass(Class<T> superClass) {
        return this.classes.stream().filter(classInfo -> classInfo.extendsSuperclass(superClass)).map(classInfo -> classInfo.loadClass(superClass)).toList();
    }

    public <T> List<Class<T>> getClassesWithInterface(Class<T> interfaceClass) {
        return this.classes.stream().filter(classInfo -> classInfo.implementsInterface(interfaceClass)).map(classInfo -> classInfo.loadClass(interfaceClass)).toList();
    }

    public List<Class<?>> getClassesWithAnnotation(Class<? extends Annotation> annotation) {
        List<Class<?>> list = new ArrayList<>();
        for (ClassInfo classInfo : this.classes) {
            if (classInfo.hasAnnotation(annotation)) {
                Class<?> loadClass = classInfo.loadClass();
                list.add(loadClass);
            }
        }
        return list;
    }


}
