package org.javlo.helper.json;

import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.deser.ValueInstantiator;
import tools.jackson.databind.deser.ValueInstantiators;
import tools.jackson.databind.deser.std.StdValueInstantiator;
import tools.jackson.databind.ext.javatime.deser.*;
import tools.jackson.databind.ext.javatime.deser.key.*;
import tools.jackson.databind.ext.javatime.ser.*;
import tools.jackson.databind.ext.javatime.ser.key.ZonedDateTimeKeySerializer;
import tools.jackson.databind.introspect.AnnotatedClass;
import tools.jackson.databind.introspect.AnnotatedClassResolver;
import tools.jackson.databind.introspect.AnnotatedMethod;
import tools.jackson.databind.module.SimpleDeserializers;
import tools.jackson.databind.module.SimpleKeyDeserializers;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.module.SimpleSerializers;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.time.*;
import java.util.Iterator;

public class JavaTimeModule extends SimpleModule {

    private static final long serialVersionUID = 1L;

    public JavaTimeModule() {
        super();
    }

    public void setupModule(JacksonModule.SetupContext context) {
        super.setupModule(context);
        SimpleDeserializers desers = new SimpleDeserializers();
        desers.addDeserializer(Instant.class, InstantDeserializer.INSTANT);
        desers.addDeserializer(OffsetDateTime.class, InstantDeserializer.OFFSET_DATE_TIME);
        desers.addDeserializer(ZonedDateTime.class, InstantDeserializer.ZONED_DATE_TIME);
        desers.addDeserializer(Duration.class, DurationDeserializer.INSTANCE);
        desers.addDeserializer(LocalDateTime.class, LocalDateTimeDeserializer.INSTANCE);
        desers.addDeserializer(LocalDate.class, LocalDateDeserializer.INSTANCE);
        desers.addDeserializer(LocalTime.class, LocalTimeDeserializer.INSTANCE);
        desers.addDeserializer(MonthDay.class, MonthDayDeserializer.INSTANCE);
        desers.addDeserializer(OffsetTime.class, OffsetTimeDeserializer.INSTANCE);
        desers.addDeserializer(Period.class, JSR310StringParsableDeserializer.PERIOD);
        desers.addDeserializer(Year.class, YearDeserializer.INSTANCE);
        desers.addDeserializer(YearMonth.class, YearMonthDeserializer.INSTANCE);
        desers.addDeserializer(ZoneId.class, JSR310StringParsableDeserializer.ZONE_ID);
        desers.addDeserializer(ZoneOffset.class, JSR310StringParsableDeserializer.ZONE_OFFSET);
        context.addDeserializers(desers);
        if (this._deserializers != null) {
            context.addDeserializers(this._deserializers);
        }

        SimpleSerializers sers = new SimpleSerializers();
        sers.addSerializer(Duration.class, DurationSerializer.INSTANCE);
        sers.addSerializer(Instant.class, InstantSerializer.INSTANCE);
        sers.addSerializer(LocalDateTime.class, LocalDateTimeSerializer.INSTANCE);
        sers.addSerializer(LocalDate.class, LocalDateSerializer.INSTANCE);
        sers.addSerializer(LocalTime.class, LocalTimeSerializer.INSTANCE);
        sers.addSerializer(MonthDay.class, MonthDaySerializer.INSTANCE);
        sers.addSerializer(OffsetDateTime.class, OffsetDateTimeSerializer.INSTANCE);
        sers.addSerializer(OffsetTime.class, OffsetTimeSerializer.INSTANCE);
        sers.addSerializer(Period.class, new ToStringSerializer(Period.class));
        sers.addSerializer(Year.class, YearSerializer.INSTANCE);
        sers.addSerializer(YearMonth.class, YearMonthSerializer.INSTANCE);
        sers.addSerializer(ZonedDateTime.class, ZonedDateTimeSerializer.INSTANCE);
        sers.addSerializer(ZoneId.class, new ZoneIdSerializer());
        sers.addSerializer(ZoneOffset.class, new ToStringSerializer(ZoneOffset.class));
        context.addSerializers(sers);
        if (this._serializers != null) {
            context.addSerializers(this._serializers);
        }

        SimpleSerializers keySers = new SimpleSerializers();
        keySers.addSerializer(ZonedDateTime.class, ZonedDateTimeKeySerializer.INSTANCE);
        context.addKeySerializers(keySers);
        if (this._keySerializers != null) {
            context.addKeySerializers(this._keySerializers);
        }

        SimpleKeyDeserializers keyDesers = new SimpleKeyDeserializers();
        keyDesers.addDeserializer(Duration.class, DurationKeyDeserializer.INSTANCE);
        keyDesers.addDeserializer(Instant.class, InstantKeyDeserializer.INSTANCE);
        keyDesers.addDeserializer(LocalDateTime.class, LocalDateTimeKeyDeserializer.INSTANCE);
        keyDesers.addDeserializer(LocalDate.class, LocalDateKeyDeserializer.INSTANCE);
        keyDesers.addDeserializer(LocalTime.class, LocalTimeKeyDeserializer.INSTANCE);
        keyDesers.addDeserializer(MonthDay.class, MonthDayKeyDeserializer.INSTANCE);
        keyDesers.addDeserializer(OffsetDateTime.class, OffsetDateTimeKeyDeserializer.INSTANCE);
        keyDesers.addDeserializer(OffsetTime.class, OffsetTimeKeyDeserializer.INSTANCE);
        keyDesers.addDeserializer(Period.class, PeriodKeyDeserializer.INSTANCE);
        keyDesers.addDeserializer(Year.class, YearKeyDeserializer.INSTANCE);
        keyDesers.addDeserializer(YearMonth.class, YearMonthKeyDeserializer.INSTANCE);
        keyDesers.addDeserializer(ZonedDateTime.class, ZonedDateTimeKeyDeserializer.INSTANCE);
        keyDesers.addDeserializer(ZoneId.class, ZoneIdKeyDeserializer.INSTANCE);
        keyDesers.addDeserializer(ZoneOffset.class, ZoneOffsetKeyDeserializer.INSTANCE);
        context.addKeyDeserializers(keyDesers);
        if (this._keyDeserializers != null) {
            context.addKeyDeserializers(this._keyDeserializers);
        }

        context.addValueInstantiators(new ValueInstantiators.Base() {
            public ValueInstantiator findValueInstantiator(DeserializationConfig config, BeanDescription beanDesc, ValueInstantiator defaultInstantiator) {
                JavaType type = beanDesc.getType();
                Class<?> raw = type.getRawClass();
                if (ZoneId.class.isAssignableFrom(raw) && defaultInstantiator instanceof StdValueInstantiator) {
                    StdValueInstantiator inst = (StdValueInstantiator)defaultInstantiator;
                    AnnotatedClass ac;
                    if (raw == ZoneId.class) {
                        ac = beanDesc.getClassInfo();
                    } else {
                        ac = AnnotatedClassResolver.resolve(config, config.constructType(ZoneId.class), config);
                    }
                    if (!inst.canCreateFromString()) {
                        AnnotatedMethod factory = JavaTimeModule.this._findFactory(ac, "of", String.class);
                        if (factory != null) {
                            inst.configureFromStringCreator(factory);
                        }
                    }
                }

                return defaultInstantiator;
            }
        });
    }

    protected AnnotatedMethod _findFactory(AnnotatedClass cls, String name, Class<?>... argTypes) {
        int argCount = argTypes.length;
        Iterator var5 = cls.getFactoryMethods().iterator();

        AnnotatedMethod method;
        do {
            if (!var5.hasNext()) {
                return null;
            }

            method = (AnnotatedMethod)var5.next();
        } while(!name.equals(method.getName()) || method.getParameterCount() != argCount);

        for(int i = 0; i < argCount; ++i) {
            Class<?> argType = method.getParameter(i).getRawType();
            if (!argType.isAssignableFrom(argTypes[i])) {
            }
        }

        return method;
    }


}
