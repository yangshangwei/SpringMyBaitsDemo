package com.artisan.dao.mybatis;

import com.artisan.domain.Artisan;

public interface ArtisanMybatisDao {

	Artisan selectArtisan(String artisanName);
}
