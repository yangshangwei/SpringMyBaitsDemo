package com.artisan.dao.mybatis;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.artisan.domain.Artisan;

public class ArtisanMybatisDaoImplTest {

	ClassPathXmlApplicationContext ctx = null;
	ArtisanMybatisDaoImpl artisanMybatisDaoImpl = null;

	@Before
	public void initContext() {
		ctx = new ClassPathXmlApplicationContext(
				"spring/applicationContext-mybatis.xml");
		artisanMybatisDaoImpl = ctx.getBean("artisanMybatisDaoImpl",
				ArtisanMybatisDaoImpl.class);
	}

	@Test
	public void testSelectArtisanWithMybatis() {
		Artisan artisan = artisanMybatisDaoImpl.selectArtisan("artisan");
		System.out.println("Artisan Id:" + artisan.getArtisanId());
		System.out.println("Artisan Name:" + artisan.getArtisanName());
		System.out.println("Artisan Desc:" + artisan.getArtisanDesc());
	}

	@After
	public void releaseContext() {
		if (ctx != null) {
			ctx.close();
		}
	}
}
