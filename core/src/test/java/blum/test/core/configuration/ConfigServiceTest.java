package blum.test.core.configuration;

import blum.core.configuration.CoreConfigurationService;
import blum.api.configuration.ConfigurationRoot;
import blum.core.configuration.loader.ConfigurationLoader;
import blum.core.configuration.loader.ReflectionHelper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfigServiceTest {

    private CoreConfigurationService service;
    private ConfigurationLoader loaderMock;

    @BeforeEach
    void setup() {
        loaderMock = Mockito.mock(ConfigurationLoader.class);
        service = new CoreConfigurationService(loaderMock, new ReflectionHelper());
    }

    @Test
    void testStartLoadsConfigurations() throws Exception {
        Map<String, Config> mockConfigs = new HashMap<>();
        mockConfigs.put("example", ConfigFactory.parseString("someValue=\"hello\""));

        when(loaderMock.loadConfigurations(any())).thenReturn(mockConfigs);

        assertDoesNotThrow(() -> service.start());
        assertTrue(service.hasConfiguration("example"));
        assertEquals(1, service.getConfigurationCount());
    }

    @Test
    void testReload() throws Exception {
        Map<String, Config> firstLoad = new HashMap<>();
        firstLoad.put("conf1", ConfigFactory.parseString("v=1"));
        Map<String, Config> secondLoad = new HashMap<>();
        secondLoad.put("conf1", ConfigFactory.parseString("v=2"));
        when(loaderMock.loadConfigurations(any()))
                .thenReturn(firstLoad)
                .thenReturn(secondLoad);

        service.start();
        assertEquals(1, service.getConfigurationCount());
        service.reload();
        assertEquals(1, service.getConfigurationCount());
        assertTrue(service.hasConfiguration("conf1"));
    }

    @Test
    void testLoadConfiguration() throws Exception {
        Map<String, Config> mockConfigs = new HashMap<>();
        mockConfigs.put("testConfig", ConfigFactory.parseString("value=42"));
        when(loaderMock.loadConfigurations(any())).thenReturn(mockConfigs);

        service.start();

        TestConfig config = service.loadConfiguration("testConfig", TestConfig.class);
        assertNotNull(config);
        assertEquals("testConfig", config.file);
        assertEquals(42, config.value);
    }

    @Test
    void testHasConfiguration() throws Exception {
        Map<String, Config> mockConfigs = new HashMap<>();
        mockConfigs.put("confA", ConfigFactory.empty());
        when(loaderMock.loadConfigurations(any())).thenReturn(mockConfigs);

        service.start();
        assertTrue(service.hasConfiguration("confA"));
        assertFalse(service.hasConfiguration("confB"));
    }

    @Test
    void testStopClearsConfigurations() throws Exception {
        Map<String, Config> mockConfigs = new HashMap<>();
        mockConfigs.put("conf", ConfigFactory.empty());
        when(loaderMock.loadConfigurations(any())).thenReturn(mockConfigs);

        service.start();
        assertEquals(1, service.getConfigurationCount());
        service.stop();
        assertEquals(0, service.getConfigurationCount());
    }

    public static class TestConfig extends ConfigurationRoot {
        public String file = "testConfig";
        public int value;

        public TestConfig(String file) {
            super(file);
        }
    }
}
