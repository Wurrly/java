package com.jsoniter.spi;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Map;

public class Binding {
    // input
    public final Class clazz;
    public final TypeLiteral clazzTypeLiteral;
    
    public Annotation[] annotations;
    public Field field; // obj.XXX
    public Method method; // obj.setXXX() or obj.getXXX()
    public boolean valueCanReuse;
    // input/output
    public String name;
    public Type valueType;
    public TypeLiteral valueTypeLiteral;
    // output
    public String[] fromNames; // for decoder
    public String[] toNames; // for encoder
    public Decoder decoder;
    public Encoder encoder;
    public boolean asMissingWhenNotPresent;
    public boolean asExtraWhenPresent;
    public boolean isNullable = true;
    public boolean isCollectionValueNullable = true;
    public boolean shouldOmitNull = true;
    // then this property will not be unknown
    // but we do not want to bind it anywhere
    public boolean shouldSkip;
    // attachment, used when generating code or reflection
    public int idx;
    public long mask;

//    public Binding(Class clazz, Map<String, Type> lookup, Type valueType) {
//        this.clazz = clazz;
//        this.clazzTypeLiteral = TypeLiteral.create(clazz);
//        this.valueType = substituteTypeVariables(lookup, valueType);
//        this.valueTypeLiteral = TypeLiteral.create(this.valueType);
//    }
    
    public Binding(Class clazz, Map<String, Type> lookup, Type valueType, Class viewClazz) {
    	
    	 
    	System.out.println("Making binding for " + clazz + "\nlookup: " + lookup + "\nvalueType: " + valueType + "\nview: " + viewClazz);
        this.clazz = clazz;
        this.clazzTypeLiteral = TypeLiteral.create(clazz,viewClazz);
        
        if( valueType.getTypeName().contains("java.lang"))
    	{
    		viewClazz = null;
    	}
        
        this.valueType = substituteTypeVariables(lookup, valueType, viewClazz);
//	    if(this.valueType instanceof ParameterizedType)
//	    {
//	    	this.valueType = new ParameterizedTypeImpl(new Type[]{valueType},null, viewClazz);
//	    }
        this.valueTypeLiteral = TypeLiteral.create(this.valueType,viewClazz);
    }

    public String decoderCacheKey() {
        return this.name + "@" + this.clazzTypeLiteral.getDecoderCacheKey();
    }

    public String encoderCacheKey() {
        return this.name + "@" + this.clazzTypeLiteral.getEncoderCacheKey();
    }

    private static Type substituteTypeVariables(Map<String, Type> lookup, Type type, Class viewClazz) {
        if (type instanceof TypeVariable) {
            return translateTypeVariable(lookup, (TypeVariable) type);
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            Type[] args = pType.getActualTypeArguments();
            
            System.err.println("pType: " + pType.getTypeName() + "\nargs before: " +  Arrays.toString(args));

            for (int i = 0; i < args.length; i++) {
                 args[i] = substituteTypeVariables(lookup, args[i],null );
                
            //    args[i] = new ParameterizedTypeImpl(new Type[]{   args[i]},null, viewClazz);
                 System.err.println("pType: " + pType.getTypeName() + "\nargs after: " +  Arrays.toString(args));

            }
            
            System.err.println("args: " + Arrays.toString(args) + " owner: " + pType.getOwnerType() + " raw: " + pType.getRawType());
            
            
            return new ParameterizedTypeImpl(args, pType.getOwnerType(), pType.getRawType());
        }
        if (type instanceof GenericArrayType) {
            GenericArrayType gaType = (GenericArrayType) type;
            return new GenericArrayTypeImpl(substituteTypeVariables(lookup, gaType.getGenericComponentType(),viewClazz));
        }
        return type;
    }

    private static Type translateTypeVariable(Map<String, Type> lookup, TypeVariable var) {
        GenericDeclaration declaredBy = var.getGenericDeclaration();
        if (!(declaredBy instanceof Class)) {
            // if the <T> is not defined by class, there is no way to get the actual type
            return Object.class;
        }
        Class clazz = (Class) declaredBy;
        Type actualType = lookup.get(var.getName() + "@" + clazz.getCanonicalName());
        if (actualType == null) {
            // should not happen
            return Object.class;
        }
        if (actualType instanceof TypeVariable) {
            // translate to another variable, try again
            return translateTypeVariable(lookup, (TypeVariable) actualType);
        }
        return actualType;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        if (annotations == null) {
            return null;
        }
        for (Annotation annotation : annotations) {
            if (annotationClass.isAssignableFrom(annotation.getClass())) {
                return (T) annotation;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Binding binding = (Binding) o;

        if (clazz != null ? !clazz.equals(binding.clazz) : binding.clazz != null) return false;
        return name != null ? name.equals(binding.name) : binding.name == null;
    }

    @Override
    public int hashCode() {
        int result = clazz != null ? clazz.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Binding{" +
                "clazz=" + clazz +
                ", name='" + name + '\'' +
                ", valueType=" + valueType +
                ", class=" + clazz +
                ", classLiteral=" + clazzTypeLiteral +
                ", valueTypeLiteral=" + valueTypeLiteral +
                '}';
    }
}
