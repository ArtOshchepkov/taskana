package org.taskana.model.mappings;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.mapping.FetchType;
import org.taskana.model.Workbasket;

public interface WorkbasketMapper {

	@Select("SELECT ID, TENANT_ID, CREATED, MODIFIED, NAME, DESCRIPTION, OWNER FROM WORKBASKET WHERE ID = #{id}")
	@Results(value = { 
			@Result(property = "id", column = "ID"), 
			@Result(property = "tenantId", column = "TENANT_ID"),
			@Result(property = "created", column = "CREATED"), 
			@Result(property = "modified", column = "MODIFIED"),
			@Result(property = "name", column = "NAME"), 
			@Result(property = "description", column = "DESCRIPTION"),
			@Result(property = "owner", column = "OWNER"),
			@Result(property = "distributionTargets", column = "ID", javaType = List.class, many = @Many(fetchType = FetchType.DEFAULT, select="findByDistributionTargets")) })
	public Workbasket findById(@Param("id") String id);
	
	@Select("SELECT * FROM WORKBASKET WHERE id IN (SELECT TARGET_ID FROM DISTRIBUTION_TARGETS WHERE SOURCE_ID = #{id})")
	@Results(value = { 
			@Result(property = "id", column = "ID"), 
			@Result(property = "tenantId", column = "TENANT_ID"),
			@Result(property = "created", column = "CREATED"), 
			@Result(property = "modified", column = "MODIFIED"),
			@Result(property = "name", column = "NAME"), 
			@Result(property = "description", column = "DESCRIPTION"),
			@Result(property = "owner", column = "OWNER"),
			@Result(property = "distributionTargets", column = "ID", javaType = List.class, many = @Many(fetchType = FetchType.DEFAULT, select="findByDistributionTargets")) })
	public List<Workbasket> findByDistributionTargets(@Param("id") String id);
	
	@Select("Select * FROM WORKBASKET ORDER BY id")
	@Results(value = { 
			@Result(property = "id", column = "ID"), 
			@Result(property = "tenantId", column = "TENANT_ID"),
			@Result(property = "created", column = "CREATED"), 
			@Result(property = "modified", column = "MODIFIED"),
			@Result(property = "name", column = "NAME"), 
			@Result(property = "description", column = "DESCRIPTION"),
			@Result(property = "owner", column = "OWNER"),
			@Result(property = "distributionTargets", column = "ID", javaType = List.class, many = @Many(fetchType = FetchType.DEFAULT, select="findByDistributionTargets")) })
	public List<Workbasket> findAll();

	@Insert("INSERT INTO WORKBASKET (ID, TENANT_ID, CREATED, MODIFIED, NAME, DESCRIPTION, OWNER) VALUES (#{workbasket.id}, #{workbasket.tenantId}, #{workbasket.created}, #{workbasket.modified}, #{workbasket.name}, #{workbasket.description}, #{workbasket.owner})")
	@Options(keyProperty = "id", keyColumn="ID")
	public void insert(@Param("workbasket") Workbasket workbasket);

	@Update("UPDATE WORKBASKET SET TENANT_ID = #{workbasket.tenantId}, MODIFIED = #{workbasket.modified}, NAME = #{workbasket.name}, DESCRIPTION = #{workbasket.description}, OWNER = #{workbasket.owner} WHERE id = #{workbasket.id}")
	public void update(@Param("workbasket") Workbasket workbasket);

	@Delete("DELETE FROM WORKBASKET where id = #{id}")
	public void delete(@Param("id") String id);

}