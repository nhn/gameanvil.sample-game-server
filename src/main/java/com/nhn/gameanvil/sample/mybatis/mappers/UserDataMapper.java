package com.nhn.gameanvil.sample.mybatis.mappers;

import com.nhn.gameanvil.sample.mybatis.dto.UserDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 유저 DB 처리 하는 맵퍼 선언
 */
@Mapper
public interface UserDataMapper {
    int insertUser(UserDto userDto);
    UserDto selectUserByUuid(@Param("uuid")final String uuid);
    int updateUserCurrentDeck(@Param("uuid") final String uuid, @Param("currentDeck") final String currentDeck);
    int updateUserNickname(@Param("uuid") final String uuid, @Param("nickname") final String nickname);
    int updateUserHighScore(@Param("uuid") final String uuid, @Param("highScore") final int highScore);
}
