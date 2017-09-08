package TagGallery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import TagGallery.entity.Album;
import TagGallery.entity.Tag;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

/**
 * Created by George-Lenovo on 6/28/2017.
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    @Query("select t from Tag t inner join t.images i where i.albumHolder = :album")
    Set<Tag> findTagsForAlbum(@Param("album") Album album);

    Tag findByTagName(String tagName);

    @Query("select t from Tag t inner join t.images i where i.id = :imageId")
    List<Tag> findTagsByImage(@Param("imageId") Long id);

    @Query(value = "select t.id\n" +
            "from Tag t\n" +
            "inner join images_tags it\n" +
            "on it.tag_id = t.id\n" +
            "group by t.id\n" +
            "order by count(it.image_id) desc\n" +
            "limit 12", nativeQuery = true)
    List<BigInteger> getTopTags();

    @Query("select t from Tag t where t.id in :ids")
    List<Tag> findByIdIn(@Param("ids") List<Long> ids);

    @Query("select t from Tag t order by id asc")
    List<Tag> findByIdAsc();


}
