package com.example.yenanow.gallery.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QNcut is a Querydsl query type for Ncut
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNcut extends EntityPathBase<Ncut> {

    private static final long serialVersionUID = -1120889445L;

    public static final QNcut ncut = new QNcut("ncut");

    public final NumberPath<Integer> commentCount = createNumber("commentCount", Integer.class);

    public final StringPath content = createString("content");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final BooleanPath isRelay = createBoolean("isRelay");

    public final NumberPath<Integer> likeCount = createNumber("likeCount", Integer.class);

    public final StringPath ncutUrl = createString("ncutUrl");

    public final StringPath ncutUuid = createString("ncutUuid");

    public final StringPath thumbnailUrl = createString("thumbnailUrl");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final StringPath userUuid = createString("userUuid");

    public final EnumPath<Visibility> visibility = createEnum("visibility", Visibility.class);

    public QNcut(String variable) {
        super(Ncut.class, forVariable(variable));
    }

    public QNcut(Path<? extends Ncut> path) {
        super(path.getType(), path.getMetadata());
    }

    public QNcut(PathMetadata metadata) {
        super(Ncut.class, metadata);
    }

}

