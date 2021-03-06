package basic.serialization;

import java.io.Serializable;
import java.util.function.Function;

public interface SerializableFunction<I, O> extends Function<I, O>, Serializable {
	
}
