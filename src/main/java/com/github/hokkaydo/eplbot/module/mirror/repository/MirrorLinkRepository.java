package com.github.hokkaydo.eplbot.module.mirror.repository;

import com.github.hokkaydo.eplbot.database.CRUDRepository;
import com.github.hokkaydo.eplbot.module.mirror.model.MirrorLink;

import java.util.List;

public interface MirrorLinkRepository extends CRUDRepository<MirrorLink> {

    List<MirrorLink> readById(Long id);

    void deleteByIds(Long idA, Long idB);

    boolean exists(Long idA, Long idB);

}
