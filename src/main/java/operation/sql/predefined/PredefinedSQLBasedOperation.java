package operation.sql.predefined;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import basic.SimpleName;
import context.project.VisProjectDBContext;
import context.scheme.appliedarchive.reproducedandinsertedinstance.VisSchemeAppliedArchiveReproducerAndInserter;
import operation.AbstractOperation;
import operation.Operation;
import operation.parameter.Parameter;
import operation.sql.SQLOperationBase;

/**
 * base class for operation types that make use of the result of a sql query string:
 * 
 * the base sql query string are automatically generated by the subclasses of PredefinedSQLBasedOperation with provided parameters;
 * 
 * 1. may involve single or multiple input record data
 * 2. may involve non-sql processing on the record of the ResultSet of the base sql query
 * 		if true, the output data table populating can not be accomplished with a full sql string with INSERT INTO clause
 * 		rather, it should be populated with JDBC's insert into table method;
 * 
 * note that currently implemented predefined sql based operation types are all single input record data and involving non-sql processing step;
 * @author tanxu
 * 
 */
public abstract class PredefinedSQLBasedOperation extends SQLOperationBase {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6569885284309423895L;

	////////////////////////////////
	/**
	 * static method to build the level specific parameter name value object map with explicit parameter values
	 * @param name
	 * @param notes
	 * @return
	 */
	public static Map<SimpleName, Object> buildPredefinedSQLBasedOperationLevelSpecificParameterNameValueObjectMap(){
		return new LinkedHashMap<>();
	}
	
	////////////////////
	//return all Parameters defined at the SQLOperation level
	private static Map<SimpleName, Parameter<?>> levelSpecificParameterNameMap(){
		return new LinkedHashMap<>();
	}
	
	////////////////////////////////
	
	////=====final fields
	private final Map<SimpleName, Object> levelSpecificParameterObjectValueMap;
	
//	/**
//	 * base sql query string of PredefinedSQLOperation need to be built by the each final subclass of {@link PredefinedSQLOperation}
//	 */
//	protected transient String baseSqlQueryString;
//	/**
//	 * RelationalTableSchema for output data table, should be constructed in each final subclass of {@link PredefinedSQLOperation}
//	 */
//	protected transient DataTableSchema outputDataTableSchema;
//	/**
//	 * RelationalTableSchemaID of output data table of PredefinedSQLBasedOperation should be constructed in each final subclass of {@link PredefinedSQLOperation} 
//	 */
//	protected transient DataTableSchemaID outputDataTableSchemaID;//
	
	
	/**
	 * constructor
	 * @param operationLevelParameterObjectValueMap
	 * @param SQLOperationBaseLevelParameterObjectValueMap
	 * @param predefinedSQLBasedOperationLevelParameterNameObjectValueMap
	 */
	protected PredefinedSQLBasedOperation(
//			boolean resultedFromReproducing, 
			Map<SimpleName, Object> operationLevelParameterObjectValueMap,
			
			Map<SimpleName, Object> SQLOperationBaseLevelParameterObjectValueMap,
			//
			Map<SimpleName, Object> predefinedSQLBasedOperationLevelParameterNameObjectValueMap
			) {
		super(operationLevelParameterObjectValueMap, SQLOperationBaseLevelParameterObjectValueMap);		
		
		//validations
		//always first validate each value objects with the constraints implemented in the Parameter<?> object of each parameter
		for(SimpleName parameterName:levelSpecificParameterNameMap().keySet()) {
			if(!predefinedSQLBasedOperationLevelParameterNameObjectValueMap.containsKey(parameterName)) {//parameter not found in the input value map
				throw new IllegalArgumentException("given PredefinedSQLBasedOperationLevelParameterObjectValueMap does not contain the value for parameter:"+parameterName.getStringValue());
			}
		}
		
		this.levelSpecificParameterObjectValueMap = predefinedSQLBasedOperationLevelParameterNameObjectValueMap;
	}

	/**
	 * {@inheritDoc}
	 * @throws SQLException 
	 */
	@Override
	protected void validateParametersValueConstraints(boolean toCheckConstraintsRelatedWithParameterDependentOnInputDataTableContent) {
		//1. super class's constraints
		super.validateParametersValueConstraints(toCheckConstraintsRelatedWithParameterDependentOnInputDataTableContent);
		//2. level specific parameter's basic constraints defined by the Parameter class
		for(SimpleName parameterName:levelSpecificParameterNameMap().keySet()) {
			Parameter<?> parameter = levelSpecificParameterNameMap().get(parameterName);
			
			if(!parameter.validateObjectValue(levelSpecificParameterObjectValueMap.get(parameterName), toCheckConstraintsRelatedWithParameterDependentOnInputDataTableContent)){
				throw new IllegalArgumentException("invalid value object found for PredefinedSQLBasedOperationLevelParameterObjectValueMap:"+parameterName);
			}
		}
		//3. additional inter-parameter constraints involving parameters at this level
		//TODO
	}
	//////////////////////////////////////////////////////////
	
	/////////////////////////////////////////////////////////
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<SimpleName, Parameter<?>> getAllParameterNameMapOfCurrentAndAboveLevels() {
		Map<SimpleName, Parameter<?>> ret = new LinkedHashMap<>();
		ret.putAll(super.getAllParameterNameMapOfCurrentAndAboveLevels());
		ret.putAll(levelSpecificParameterNameMap());
		return ret;
	}
	
	@Override
	public Map<SimpleName, Object> getAllParameterNameValueObjectMapOfCurrentAndAboveLevels() {
		
		Map<SimpleName, Object> ret = new LinkedHashMap<>();
		ret.putAll(super.getAllParameterNameMapOfCurrentAndAboveLevels());
		ret.putAll(levelSpecificParameterNameMap());
		
		return ret;
	}
	
	
	/**
	 * since {@link AbstractOperation} is the root class of {@link Operation} hierarchy, if the given parameter is not in {@link #getLevelSpecificParameterNameMap()}, throw {@link IllegalArgumentException}
	 */
	@Override
	public void setParameterValueObject(SimpleName parameterName, Object value) {
		if(levelSpecificParameterNameMap().containsKey(parameterName)) {
			this.setLevelSpecificParameterValueObject(parameterName, value);
		}else {
			super.setParameterValueObject(parameterName, value);
		}
	}
	
	@Override
	public void setLevelSpecificParameterValueObject(SimpleName parameterName, Object value) {
//		if(!levelSpecificParameterNameMap().get(parameterName).validateObjectValue(value, this.isReproduced())) {
//			throw new IllegalArgumentException("given parameter value object is invalid:"+value);
//		}
		
		this.levelSpecificParameterObjectValueMap.put(parameterName, value);
	}
	
	///////////////////////////////////
	
	/**
	 * reproduce and return a parameter name value object map of parameters at SQLOperationBase level
	 * 
	 * @param hostVisProjctDBContext the host VisProjectDBContext to which the reproduced Operation will be inserted;
	 * @param VSAArchiveReproducerAndInserter the VSAArchiveReproducerAndInserter that triggers the reproduce process; note that the VisSchemeAppliedArchive is contained in this object
	 * @param copyIndex copy index of the VCDNode/VSComponent to which this Operation is assigned
	 * @return
	 */
	protected Map<SimpleName, Object> reproducePredefinedSQLBasedOperationLevelParameterObjectValueMap(
			VisProjectDBContext hostVisProjctDBContext,
			VisSchemeAppliedArchiveReproducerAndInserter VSAArchiveReproducerAndInserter,
			int copyIndex) {
		return new HashMap<>();
	}

	
	//////////////////////////////
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((levelSpecificParameterObjectValueMap == null) ? 0
				: levelSpecificParameterObjectValueMap.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof PredefinedSQLBasedOperation))
			return false;
		PredefinedSQLBasedOperation other = (PredefinedSQLBasedOperation) obj;
		if (levelSpecificParameterObjectValueMap == null) {
			if (other.levelSpecificParameterObjectValueMap != null)
				return false;
		} else if (!levelSpecificParameterObjectValueMap.equals(other.levelSpecificParameterObjectValueMap))
			return false;
		return true;
	}
	
	
	
	///////////////////////////
//	//abstract methods of {@link SQLOperationBase}
//	/**
//	 * construct and return the RelationalTableSchemaID for output data table
//	 */
//	@Override
//	protected DataTableSchemaID getOutputDataTableSchemaID() {
//		if(this.outputDataTableSchemaID == null) {
//			this.outputDataTableSchemaID = DataTableSchemaFactory
//					.makeDataTableSchemaID(this.getOutputDataTableName());
//		}
//		
//		return this.outputDataTableSchemaID;
//	}
	
	
	////===original abstract methods that facilitate perform() method
//	/**
//	 * build and return the RelationalTableSchema for the output data table with the given VisProjectDBContext;
//	 * the parameter value objects contained in the operation instance may not be sufficient to construct the RelationalTableSchema, some of the information need to be looked up from the rdb of the host VisProjectDBContext
//	 * 
//	 * this method is to facilitate implementation of perform() method
//	 * @return
//	 */
//	protected abstract DataTableSchema getOutputDataTableSchema();
	
//	/**
//	 * construct (if needed) and return the sql string that can generate a view of the output data table of instance of this operation type;
//	 * the parameter value objects contained in the operation instance may not be sufficient to construct the RelationalTableSchema, some of the information need to be looked up from the rdb of the host VisProjectDBContext
//	 * 
//	 * this method is to facilitate implementation of perform() method
//	 * @return
//	 */
//	protected abstract String getBaseSqlQueryString();
}
