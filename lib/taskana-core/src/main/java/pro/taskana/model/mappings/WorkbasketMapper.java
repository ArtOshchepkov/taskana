package pro.taskana.model.mappings;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;
import pro.taskana.model.Workbasket;
import pro.taskana.model.WorkbasketAuthorization;

import java.util.List;
/**
 * This class is the mybatis mapping of workbaskets.
 */
public interface WorkbasketMapper {

    @Select("SELECT ID, CREATED, MODIFIED, NAME, DESCRIPTION, OWNER FROM WORKBASKET WHERE ID = #{id}")
    @Results(value = { @Result(property = "id", column = "ID"),
            @Result(property = "created", column = "CREATED"),
            @Result(property = "modified", column = "MODIFIED"),
            @Result(property = "name", column = "NAME"),
            @Result(property = "description", column = "DESCRIPTION"),
            @Result(property = "owner", column = "OWNER"),
            @Result(property = "distributionTargets", column = "ID", javaType = List.class, many = @Many(fetchType = FetchType.DEFAULT, select = "findByDistributionTargets")) })
    Workbasket findById(@Param("id") String id);

    @Select("SELECT * FROM WORKBASKET WHERE id IN (SELECT TARGET_ID FROM DISTRIBUTION_TARGETS WHERE SOURCE_ID = #{id})")
    @Results(value = {
            @Result(property = "id", column = "ID"),
            @Result(property = "created", column = "CREATED"),
            @Result(property = "modified", column = "MODIFIED"),
            @Result(property = "name", column = "NAME"),
            @Result(property = "description", column = "DESCRIPTION"),
            @Result(property = "owner", column = "OWNER"),
            @Result(property = "distributionTargets", column = "ID", javaType = List.class, many = @Many(fetchType = FetchType.DEFAULT, select = "findByDistributionTargets")) })
    List<Workbasket> findByDistributionTargets(@Param("id") String id);

    @Select("SELECT * FROM WORKBASKET ORDER BY id")
    @Results(value = {
            @Result(property = "id", column = "ID"),
            @Result(property = "created", column = "CREATED"),
            @Result(property = "modified", column = "MODIFIED"),
            @Result(property = "name", column = "NAME"),
            @Result(property = "description", column = "DESCRIPTION"),
            @Result(property = "owner", column = "OWNER"),
            @Result(property = "distributionTargets", column = "ID", javaType = List.class, many = @Many(fetchType = FetchType.DEFAULT, select = "findByDistributionTargets")) })
    List<Workbasket> findAll();

    @Select("<script>SELECT W.ID, W.CREATED, W.MODIFIED, W.NAME, W.DESCRIPTION, W.OWNER FROM WORKBASKET AS W "
            + "INNER JOIN WORKBASKET_ACCESS_LIST AS ACL " + "ON (W.ID = ACL.WORKBASKET_ID AND ACL.ACCESS_ID = #{accessId}) "
            + "WHERE <foreach collection='authorizations' item='authorization' separator=' AND '>"
            + "<if test=\"authorization.name() == 'OPEN'\">OPEN</if>"
            + "<if test=\"authorization.name() == 'READ'\">READ</if>"
            + "<if test=\"authorization.name() == 'APPEND'\">APPEND</if>"
            + "<if test=\"authorization.name() == 'TRANSFER'\">TRANSFER</if>"
            + "<if test=\"authorization.name() == 'DISTRIBUTE'\">DISTRIBUTE</if> = 1 </foreach> "
            + "ORDER BY id</script>")
    @Results(value = {
            @Result(property = "id", column = "ID"),
            @Result(property = "created", column = "CREATED"),
            @Result(property = "modified", column = "MODIFIED"),
            @Result(property = "name", column = "NAME"),
            @Result(property = "description", column = "DESCRIPTION"),
            @Result(property = "owner", column = "OWNER"),
            @Result(property = "distributionTargets", column = "ID", javaType = List.class, many = @Many(fetchType = FetchType.DEFAULT, select = "findByDistributionTargets")) })
    List<Workbasket> findByPermission(@Param("authorizations") List<WorkbasketAuthorization> authorizations, @Param("accessId") String accessId);

    @Insert("INSERT INTO WORKBASKET (ID, CREATED, MODIFIED, NAME, DESCRIPTION, OWNER) VALUES (#{workbasket.id}, #{workbasket.created}, #{workbasket.modified}, #{workbasket.name}, #{workbasket.description}, #{workbasket.owner})")
    @Options(keyProperty = "id", keyColumn = "ID")
    void insert(@Param("workbasket") Workbasket workbasket);

    @Update("UPDATE WORKBASKET SET MODIFIED = #{workbasket.modified}, NAME = #{workbasket.name}, DESCRIPTION = #{workbasket.description}, OWNER = #{workbasket.owner} WHERE id = #{workbasket.id}")
    void update(@Param("workbasket") Workbasket workbasket);

    @Delete("DELETE FROM WORKBASKET where id = #{id}")
    void delete(@Param("id") String id);

}
