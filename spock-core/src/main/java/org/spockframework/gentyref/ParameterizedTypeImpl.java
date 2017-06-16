package org.spockframework.gentyref;

import java.lang.reflect.*;
import java.util.Arrays;

class ParameterizedTypeImpl implements ParameterizedType {
	private final Class<?> rawType;
	private final Type[] actualTypeArguments;
	private final Type ownerType;

	public ParameterizedTypeImpl(Class<?> rawType, Type[] actualTypeArguments, Type ownerType) {
		this.rawType = rawType;
		this.actualTypeArguments = actualTypeArguments;
		this.ownerType = ownerType;
	}

	@Override
  public Type getRawType() {
		return rawType;
	}

	@Override
  public Type[] getActualTypeArguments() {
		return actualTypeArguments;
	}

	@Override
  public Type getOwnerType() {
		return ownerType;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ParameterizedType))
			return false;

		ParameterizedType other = (ParameterizedType) obj;
		return rawType.equals(other.getRawType())
			&& Arrays.equals(actualTypeArguments, other.getActualTypeArguments())
			&& (ownerType == null ? other.getOwnerType() == null : ownerType.equals(other.getOwnerType()));
	}

	@Override
	public int hashCode() {
		int result = rawType.hashCode() ^ Arrays.hashCode(actualTypeArguments);
		if (ownerType != null)
			result ^= ownerType.hashCode();
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		String clazz = rawType.getName();

		if (ownerType != null) {
			sb.append(GenericTypeReflector.getTypeName(ownerType)).append('.');

			String prefix = (ownerType instanceof ParameterizedType) ? ((Class<?>)((ParameterizedType)ownerType).getRawType()).getName() + '$'
					: ((Class<?>)ownerType).getName() + '$';
			if (clazz.startsWith(prefix))
				clazz = clazz.substring(prefix.length());
		}
		sb.append(clazz);

		if(actualTypeArguments.length != 0) {
			sb.append('<');
			for (int i = 0; i < actualTypeArguments.length; i++) {
				Type arg = actualTypeArguments[i];
				if (i != 0)
					sb.append(", ");
				sb.append(GenericTypeReflector.getTypeName(arg));
			}
			sb.append('>');
		}

		return sb.toString();
	}
}
