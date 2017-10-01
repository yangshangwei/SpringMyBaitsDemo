package com.artisan.dao.mybatis;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.artisan.domain.Artisan;

/**
 * 
 * 
 * @ClassName: ArtisanMybatisDaoImpl
 * 
 * @Description: @Repository标注的DAO层，被Spring管理
 * 
 * @author: Mr.Yang
 * 
 * @date: 2017年10月1日 下午12:18:25
 */

@Repository
public class ArtisanMybatisDaoImpl implements ArtisanMybatisDao {

	private SqlSessionTemplate sqlSessionTemplate;

	// 注入SqlSessionTemplate
	@Autowired
	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		this.sqlSessionTemplate = sqlSessionTemplate;
	}

	@Override
	public Artisan selectArtisan(String artisanName) {
		// 必须是获取接口
		ArtisanMybatisDao artisanMybatisDao = sqlSessionTemplate
				.getMapper(ArtisanMybatisDao.class);
		return artisanMybatisDao.selectArtisan(artisanName);
	}

}
