package blum.test.core.configuration;

import blum.api.configuration.ConfigurationRoot;
import blum.api.annotation.Named;
import blum.core.configuration.ConfigurationMapper;
import blum.core.configuration.loader.ReflectionHelper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfigMapperTest {

    @Test
    void testParsing() throws Exception {
        // Load the base configuration (application.conf, reference.conf, etc.)
        Config conf = ConfigFactory.empty();

        // ConfigObjects are immutable -> use withValue() to add new entries
        conf = conf.withValue("testValue", ConfigValueFactory.fromAnyRef("Hello World"))
                .withValue("customName", ConfigValueFactory.fromAnyRef(17))
                .withValue("custom.dotted.name", ConfigValueFactory.fromAnyRef(27));

        // Pass the config root to your custom mapper
        ConfigurationMapper mapper = new ConfigurationMapper(new ReflectionHelper());
        TestConfigurationRoot parsed = mapper.parseConfigurationPart(conf.root(), TestConfigurationRoot.class, "Test");

        // Assertions: check that the mapper correctly picked up the values
        assertEquals(17, parsed.customFieldName);
        assertEquals("Hello World", parsed.testValue);
        assertEquals(27, parsed.customDottedName);
    }

    @Test
    void testRender() throws Exception {
        TestConfigurationRoot obj = new TestConfigurationRoot("Test");
        ConfigurationMapper mapper = new ConfigurationMapper(new ReflectionHelper());

        Config conf = mapper.toConfig(obj).toConfig();

        assertEquals(108, conf.getInt("customName"));
        assertEquals("The testable value", conf.getString("testValue"));
        assertEquals(36, conf.getInt("custom.dotted.name"));
    }

    public static class TestConfigurationRoot extends ConfigurationRoot {
        @Getter
        private String testValue = "The testable value";

        @Getter
        @Named("customName")
        private int customFieldName=108;

        @Getter
        @Named("custom.dotted.name")
        private int customDottedName=36;


        public TestConfigurationRoot(String file) {
            super(file);
        }
    }

}
