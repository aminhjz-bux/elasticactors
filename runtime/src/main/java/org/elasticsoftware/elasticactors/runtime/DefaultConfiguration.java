package org.elasticsoftware.elasticactors.runtime;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.elasticsoftware.elasticactors.ActorSystemConfiguration;
import org.elasticsoftware.elasticactors.ElasticActor;
import org.elasticsoftware.elasticactors.ServiceActor;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Joost van de Wijgerd
 */
public final class DefaultConfiguration implements ActorSystemConfiguration, ApplicationContextAware {
    private ApplicationContext applicationContext;
    private final String name;
    private final int numberOfShards;
    private final Map<String,Object> properties = new LinkedHashMap<>();
    private final ConversionService conversionService = new DefaultConversionService();

    @JsonCreator
    public DefaultConfiguration(@JsonProperty("name") String name, @JsonProperty("shards") int numberOfShards) {
        this.name = name;
        this.numberOfShards = numberOfShards;
    }

    @JsonProperty("name")
    @Override
    public String getName() {
        return name;
    }

    @JsonProperty("shards")
    @Override
    public int getNumberOfShards() {
        return numberOfShards;
    }

    @JsonAnySetter
    public void setProperty(String name,Object value) {
        this.properties.put(name,value);
    }

    @Override
    public String getVersion() {
        // @todo: fix this
        return "1.0.0";
    }

    @Override
    public ElasticActor<?> getService(String serviceId) {
        return applicationContext.getBean(serviceId,ElasticActor.class);
    }

    @Override
    public Set<String> getServices() {
        return applicationContext.getBeansWithAnnotation(ServiceActor.class).keySet();
    }

    @Override
    public <T> T getProperty(Class component, String key, Class<T> targetType) {
        Map<String,Object> componentProperties = (Map<String, Object>) properties.get(generateComponentName(component));
        if(componentProperties != null) {
            Object value = componentProperties.get(key);
            if(value != null) {

                if(conversionService.canConvert(value.getClass(),targetType)) {
                    return conversionService.convert(value,targetType);
                }
            }
        }
        return null;
    }

    @Override
    public <T> T getProperty(Class component, String key, Class<T> targetType, T defaultValue) {
        T value = getProperty(component,key,targetType);
        return (value != null) ? value : defaultValue;
    }

    @Override
    public <T> T getRequiredProperty(Class component, String key, Class<T> targetType) throws IllegalStateException {
        T value = getProperty(component,key,targetType);
        if (value == null) {
            throw new IllegalStateException(String.format("required key [%s] not found for component[%s]", key, component.getName()));
        }
        return value;
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Strip out the first two packages from {@link Class#getName()}
     *
     * @param component
     * @return
     */
    private String generateComponentName(Class component) {
        String componentName = component.getName();
        int idx = componentName.indexOf('.',componentName.indexOf('.')+1);
        if(idx != -1) {
            componentName = componentName.substring(idx+1);
        }
        return componentName;
    }
}