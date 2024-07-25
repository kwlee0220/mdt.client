package mdt.client;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultMultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import utils.CSV;
import utils.func.Try;
import utils.func.Tuple;
import utils.stream.FStream;

import lombok.experimental.UtilityClass;
import mdt.model.DataTypes;
import mdt.model.registry.ResourceNotFoundException;
import mdt.model.resource.value.MultiLanguagePropertyValue;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public class SubmodelUtils {
	public static Property newProperty(String id, DataTypeDefXsd valueType, @Nullable String value) {
		return new DefaultProperty.Builder()
									.idShort(id)
									.value(value)
									.valueType(valueType)
									.build();
	}
	
	public static Property newStringProperty(String id, @Nullable String value) {
		return newProperty(id, DataTypes.STRING.getTypeDefXsd(), value);
	}
	public static Property newStringProperty(String id, @Nullable Object value) {
		return newProperty(id, DataTypes.STRING.getTypeDefXsd(), (value != null) ? value.toString() : null);
	}

	public static Property newIntProperty(String id, @Nullable int value) {
		return newProperty(id, DataTypes.INTEGER.getTypeDefXsd(), DataTypes.INTEGER.toValueString(value));
	}

	public static Property newLongProperty(String id, @Nullable Long value) {
		return newProperty(id, DataTypes.LONG.getTypeDefXsd(), DataTypes.LONG.toValueString(value));
	}

	public static Property newDateTimeProperty(String id, @Nullable Instant value) {
		return newProperty(id, DataTypes.DATE_TIME.getTypeDefXsd(), DataTypes.DATE_TIME.toValueString(value));
	}

	public static Property newDateProperty(String id, @Nullable Date value) {
		return newProperty(id, DataTypes.DATE.getTypeDefXsd(), DataTypes.DATE.toValueString(value));
	}

	public static Property newDurationProperty(String id, @Nullable Duration value) {
		return newProperty(id, DataTypes.DURATION.getTypeDefXsd(), DataTypes.DURATION.toValueString(value));
	}

	public static MultiLanguageProperty newMultiLanguageProperty(String id, MultiLanguagePropertyValue value) {
		List<LangStringTextType> textList
				= FStream.from(value.get())
							.map(langText -> new DefaultLangStringTextType.Builder()
																			.language(langText.getLanguage())
																			.text(langText.getText())
																			.build())
							.cast(LangStringTextType.class)
							.toList();
		return new DefaultMultiLanguageProperty.Builder()
						.idShort(id)
						.value(textList)
						.build();
	}
	
	public static FStream<String> parseIdShortPath(String idShortPath) {
		return CSV.parseCsv(idShortPath, '.')
					.flatMapIterable(seg -> parsePathSegment(seg));
	}
	
	public static String buildIdShortPath(Iterable<String> segList) {
		StringBuilder builder = new StringBuilder();
		for ( String seg: segList ) {
			try {
				int idx = Integer.parseInt(seg);
				builder = builder.append(String.format("[%d]", idx));
			}
			catch ( NumberFormatException e ) {
				if ( !builder.isEmpty() ) {
					builder = builder.append('.');
				}
				builder = builder.append(seg);
			}
		}
		
		return builder.toString();
	}
	
	public static <T> T getPropertyValueByPath(Submodel submodel, String idShortPath, Class<T> valueClass)
		throws ResourceNotFoundException {
		Property prop = cast(traverse(submodel, idShortPath), Property.class);
		return getPropertyValue(prop, valueClass);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getPropertyValue(Property prop, Class<T> cls) {
		Object value = DataTypes.fromAas4jDatatype(prop.getValueType()).parseValueString(prop.getValue());
		if ( cls.isAssignableFrom(value.getClass()) ) {
			return (T)value;
		}
		else {
			throw new IllegalArgumentException("Cannot cast to " + cls);
		}
	}

	public static <T> T getPropertyValueByPath(SubmodelElement start, String idShortPath, Class<T> valueClass)
		throws ResourceNotFoundException {
		Property prop = cast(traverse(start, idShortPath), Property.class);
		return getPropertyValue(prop, valueClass);
	}
	
	public static SubmodelElement traverse(SubmodelElement root, String idShortPath) {
		SubmodelElement current = root;
		for ( String seg: CSV.parseCsv(idShortPath, '.')
								.flatMapIterable(seg -> parsePathSegment(seg)) ) {
			current = hop(current, seg);
			if ( current == null ) {
				break;
			}
		}
		
		return current;
	}
	
	public static SubmodelElement traverse(Submodel submodel, String idShortPath) throws ResourceNotFoundException {
		Preconditions.checkNotNull(submodel, "Submodel was null");
		Preconditions.checkNotNull(idShortPath, "idShortPath was null");
		
		SubmodelElementCollection start = new DefaultSubmodelElementCollection.Builder()
													.value(submodel.getSubmodelElements())
													.build();
		return traverse(start, idShortPath);
//		
//		return FStream.from(submodel.getSubmodelElements())
//						.map(sme -> traverse(sme, idShortPath))
//						.findFirst(sme -> sme != null)
//						.getOrNull();
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends SubmodelElement> T cast(SubmodelElement sme, Class<T> toClass) {
		if ( toClass.isAssignableFrom(sme.getClass()) ) {
			return (T)sme;
		}
		else {
			throw new IllegalArgumentException("Cannot cast to " + toClass);
		}
	}
	
	public static Tuple<String,List<String>> parseModelReference(Reference ref) {
		if ( ref.getType() == ReferenceTypes.MODEL_REFERENCE ) {
			List<Key> keys = ref.getKeys();
			if ( keys.get(0).getType() != KeyTypes.SUBMODEL ) {
				String msg = String.format("Unexpected type for the first key: type=%s", keys.get(0).getType());
				throw new IllegalArgumentException(msg);
			}
			
			ArrayList<String> pathSegs = Lists.newArrayList();
			for ( int i =0; i < keys.size(); ++i ) {
				Key key = keys.get(i);
				switch ( key.getType() ) {
					case ASSET_ADMINISTRATION_SHELL:
					case IDENTIFIABLE:
					case MULTI_LANGUAGE_PROPERTY:
					case REFERABLE:
					case SUBMODEL:
						String msg = String.format("Unexpected type: type=%s", key.getType());
						throw new IllegalArgumentException(msg);
					default:
						try {
							int idx = Integer.parseInt(key.getValue());
							if ( i > 1 ) {
								if ( keys.get(i).getType() == KeyTypes.SUBMODEL_ELEMENT_LIST ) {
									String last = pathSegs.remove(pathSegs.size()-1);
									last = String.format("%s[%d]", last, idx);
									pathSegs.add(last);
								}
							}
							else {
								String msg2 = String.format("Invalid reference key=%s", key.getValue());
								throw new IllegalArgumentException(msg2);
							}
						}
						catch ( NumberFormatException e ) {
							pathSegs.add(key.getValue());
						}
						break;
				}
			}
			
			return Tuple.of(keys.get(0).getValue(), pathSegs);
		}
		else {
			String msg = String.format("Not ModelReference: type=%s", ref.getType());
			throw new IllegalArgumentException(msg);
		}
	}
	

	private static @Nullable SubmodelElement hop(SubmodelElement sme, String seg) {
		if ( sme instanceof SubmodelElementCollection smc ) {
			return FStream.from(smc.getValue())
							.findFirst(e ->  e.getIdShort() != null && e.getIdShort().equals(seg))
							.getOrNull();
		}
		else if ( sme instanceof SubmodelElementList sml ) {
			// Navigate 대상이 SubmodelElementList인 경우에는 'seg'가 숫자일 수도 있기 때문에
			// integer로의 파싱을 시도한다. 만일 숫자가 아닌 경우에는 'idx'가 -1로 설정된다.
			int idx = Try.get(() -> Integer.parseInt(seg)).getOrElse(-1);
			if ( idx >= 0 ) {
				return sml.getValue().get(idx);
			}
			else {
				return FStream.from(sml.getValue())
								.findFirst(e ->  e.getIdShort() != null && e.getIdShort().equals(seg))
								.getOrNull();
			}
		}
		
		return null;
	}

	private static final Pattern PATTERN = Pattern.compile("(\\w*)(\\[(d+)\\])?");
	private static List<String> parsePathSegment(String idShort) {
		Matcher matcher = PATTERN.matcher(idShort);
		List<String> matches = Lists.newArrayList();
		while ( matcher.find() ) {
			matches.add(matcher.group());
		}
		
		return switch ( matches.size() ) {
			case 0 -> matches;
			case 2 -> List.of(matches.get(0));
			case 4 -> List.of(matches.get(1));
			case 5 -> List.of(matches.get(1), matches.get(2));
			default -> throw new AssertionError();
		};
	}
}
