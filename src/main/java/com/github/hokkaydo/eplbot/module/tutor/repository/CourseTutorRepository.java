package com.github.hokkaydo.eplbot.module.tutor.repository;

import com.github.hokkaydo.eplbot.database.CRUDRepository;
import com.github.hokkaydo.eplbot.module.tutor.model.CourseTutor;

import java.util.List;

public interface CourseTutorRepository extends CRUDRepository<CourseTutor> {

    List<CourseTutor> readByChannelId(Long channelId);

    List<CourseTutor> readByTutorId(Long tutorId);

    void deleteByChannelId(Long channelId);

    void updatePing(Long channelId, Long tutorId, boolean allowPing);

}
