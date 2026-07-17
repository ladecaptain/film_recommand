package com.film.config;

import com.film.entity.Movie;
import com.film.mapper.MovieMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
public class DataSeeder implements ApplicationRunner {

    private final MovieMapper movieMapper;

    public DataSeeder(MovieMapper movieMapper) {
        this.movieMapper = movieMapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        long count = movieMapper.selectCount(null);
        if (count > 0) {
            log.info("数据库已有 {} 部电影，跳过种子数据", count);
            return;
        }
        log.info("数据库为空，插入种子数据...");

        List<Movie> movies = List.of(
            createMovie(550L, "搏击俱乐部", "Fight Club",
                "/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg",
                "一个患有失眠症的上班族遇到了一位肥皂推销员，他们一起创办了一个地下搏击俱乐部。",
                LocalDate.of(1999, 10, 15), 8.4, 28000, 139, "剧情,悬疑", "大卫·芬奇", "爱德华·诺顿,布拉德·皮特,海伦娜·伯翰·卡特"),

            createMovie(680L, "低俗小说", "Pulp Fiction",
                "/d5iIlFn5s0ImszYzBPb8JPIfbXD.jpg",
                "几组人物在洛杉矶的犯罪世界中交织在一起，包括两个杀手、一个拳击手、一对抢劫犯和一对小情侣。",
                LocalDate.of(1994, 10, 14), 8.5, 25000, 154, "剧情,犯罪", "昆汀·塔伦蒂诺", "约翰·特拉沃尔塔,乌玛·瑟曼,塞缪尔·杰克逊"),

            createMovie(13L, "阿甘正传", "Forrest Gump",
                "/arw2vcBveWOVZr6pxd9XTd1TdQa.jpg",
                "一个来自阿拉巴马州的善良男人，凭借着乐观的天性，在人生道路上见证了历史的变迁。",
                LocalDate.of(1994, 7, 6), 8.5, 26000, 142, "剧情,爱情", "罗伯特·泽米吉斯", "汤姆·汉克斯,罗宾·怀特,加里·西尼斯"),

            createMovie(155L, "蝙蝠侠：黑暗骑士", "The Dark Knight",
                "/qJ2tW6WMUDux911BytJyl0xQaXS.jpg",
                "蝙蝠侠在哥谭市面临着迄今为止最大的挑战——混乱的化身小丑。",
                LocalDate.of(2008, 7, 18), 8.5, 31000, 152, "动作,科幻,犯罪", "克里斯托弗·诺兰", "克里斯蒂安·贝尔,希斯·莱杰,艾伦·艾克哈特"),

            createMovie(27205L, "盗梦空间", "Inception",
                "/9gk7adHYeNL91uEEtEfdND7Fis7.jpg",
                "一名专门在人们梦境中窃取秘密的小偷，被赋予了一项任务：在目标人物的潜意识中植入一个想法。",
                LocalDate.of(2010, 7, 16), 8.4, 35000, 148, "动作,科幻,冒险", "克里斯托弗·诺兰", "莱昂纳多·迪卡普里奥,约瑟夫·高登-莱维特,渡边谦"),

            createMovie(11L, "星球大战", "Star Wars",
                "/6FfCtAuVAW8XJjZ7eWaLnZGLepw.jpg",
                "卢克·天行者加入了绝地武士的行列，与邪恶的银河帝国展开了一场史诗般的战斗。",
                LocalDate.of(1977, 5, 25), 8.2, 19000, 121, "动作,科幻,冒险", "乔治·卢卡斯", "马克·哈米尔,哈里森·福特,凯丽·费雪"),

            createMovie(122L, "指环王：王者归来", "The Lord of the Rings: The Return of the King",
                "/rCzpDGLbOoPwLjy3OAm5Nz3Eh3v.jpg",
                "弗罗多和山姆向末日山进发，而阿拉贡则带领人类最后的军队对抗索伦。",
                LocalDate.of(2003, 12, 17), 8.5, 23000, 201, "动作,冒险,奇幻", "彼得·杰克逊", "伊利亚·伍德,维果·莫特森,伊恩·麦克莱恩"),

            createMovie(244786L, "爆裂鼓手", "Whiplash",
                "/7fn624j5lj3xTme2SgiLCeuedmO.jpg",
                "一名年轻鼓手在一位严苛导师的指导下，不惜一切代价追求完美。",
                LocalDate.of(2014, 10, 10), 8.4, 14000, 106, "剧情,音乐", "达米恩·查泽雷", "迈尔斯·特勒,J·K·西蒙斯"),

            createMovie(157336L, "星际穿越", "Interstellar",
                "/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
                "一组探险者利用新发现的虫洞，试图为人类寻找新的家园。",
                LocalDate.of(2014, 11, 7), 8.4, 33000, 169, "科幻,冒险,剧情", "克里斯托弗·诺兰", "马修·麦康纳,安妮·海瑟薇,杰西卡·查斯坦"),

            createMovie(335983L, "毒液：致命守护者", "Venom",
                "/2uNW4WbgBXL25BAbyXjMxK4sD6D.jpg",
                "记者埃迪·布洛克获得了外星共生体的力量，他必须学会控制自己的新能力。",
                LocalDate.of(2018, 10, 5), 6.8, 14000, 112, "动作,科幻", "鲁本·弗雷斯彻", "汤姆·哈迪,米歇尔·威廉姆斯"),

            createMovie(118340L, "银河护卫队", "Guardians of the Galaxy",
                "/r7vmZjiyZw9rpJMQJdXpjgiCOk9.jpg",
                "一群星际罪犯联合起来保护一颗神秘的球体免受反派指控者罗南的侵害。",
                LocalDate.of(2014, 8, 1), 7.9, 26000, 121, "动作,科幻,冒险", "詹姆斯·古恩", "克里斯·帕拉特,佐伊·索尔达娜,戴夫·巴蒂斯塔"),

            createMovie(105L, "回到未来", "Back to the Future",
                "/fNOH9f1aA7XRTzl1sAOx9iF553Q.jpg",
                "一个少年意外乘坐时光机回到1955年，他必须确保父母相爱才能回到未来。",
                LocalDate.of(1985, 7, 3), 8.2, 18000, 116, "科幻,冒险,喜剧", "罗伯特·泽米吉斯", "迈克尔·J·福克斯,克里斯托弗·洛伊德"),

            createMovie(78L, "银翼杀手", "Blade Runner",
                "/63N9uy8nd9j7Eog2axPQ8lbr3Wj.jpg",
                "在未来的反乌托邦世界中，一名退休警察被召回追捕四名复制人。",
                LocalDate.of(1982, 6, 25), 8.1, 13000, 117, "科幻,悬疑", "雷德利·斯科特", "哈里森·福特,鲁特格尔·哈尔"),

            createMovie(77338L, "触不可及", "Intouchables",
                "/1QU7HKgsQbGpzsJbJK4pAVQVvF8.jpg",
                "一个瘫痪的富翁雇佣了一个来自贫民窟的年轻看护，两人建立了深厚的友谊。",
                LocalDate.of(2011, 11, 2), 8.5, 16000, 112, "剧情,喜剧", "奥利维埃·纳卡什", "弗朗索瓦·克鲁塞,奥玛·希"),

            createMovie(603L, "黑客帝国", "The Matrix",
                "/f89U3ADr1oiB1s9GkdPOEpONlUi.jpg",
                "一名黑客发现了一个令人震惊的真相：他生活的世界不过是一个虚拟现实。",
                LocalDate.of(1999, 3, 31), 8.2, 24000, 136, "动作,科幻", "沃卓斯基姐妹", "基努·里维斯,劳伦斯·菲什伯恩,凯瑞-安·莫斯"),

            createMovie(278L, "肖申克的救赎", "The Shawshank Redemption",
                "/q6y0Go1tsGEsmtFryDOJo3dEmqu.jpg",
                "一个被错判的银行家在监狱中用20年时间策划了一次大胆的越狱。",
                LocalDate.of(1994, 9, 23), 8.7, 26000, 142, "剧情", "弗兰克·德拉邦特", "蒂姆·罗宾斯,摩根·弗里曼"),

            createMovie(769L, "好家伙", "GoodFellas",
                "/aKuFiU82s5ISJDx7kLi0xN2QoJL.jpg",
                "一个年轻人从布鲁克林街头爬上了黑帮的高层，但最终面临背叛和毁灭。",
                LocalDate.of(1990, 9, 19), 8.5, 12000, 145, "剧情,犯罪", "马丁·斯科塞斯", "罗伯特·德尼罗,雷·利奥塔,乔·佩西"),

            createMovie(120467L, "布达佩斯大饭店", "The Grand Budapest Hotel",
                "/eWdyYQreja6JGCzqHWXpWHDrrPo.jpg",
                "一个传奇酒店礼宾和他最信任的门童之间的冒险故事。",
                LocalDate.of(2014, 3, 28), 8.1, 13800, 99, "喜剧,剧情", "韦斯·安德森", "拉尔夫·费因斯,托尼·雷沃洛利"),

            createMovie(238L, "教父", "The Godfather",
                "/3bhkrj58Vtu7enYsRolD1fZdja1.jpg",
                "维托·柯里昂是纽约最有权势的黑手党家族首领，他的小儿子迈克尔被迫卷入家族事业。",
                LocalDate.of(1972, 3, 24), 8.7, 19800, 175, "剧情,犯罪", "弗朗西斯·福特·科波拉", "马龙·白兰度,阿尔·帕西诺,詹姆斯·凯恩"),

            createMovie(424L, "辛德勒的名单", "Schindler's List",
                "/sF1U4EUQS8YHUYjNl3pMGNIQyr0.jpg",
                "德国商人奥斯卡·辛德勒在二战期间拯救了1100多名犹太人的生命。",
                LocalDate.of(1993, 12, 15), 8.6, 15000, 195, "剧情,历史,战争", "史蒂文·斯皮尔伯格", "连姆·尼森,本·金斯利,拉尔夫·费因斯")
        );

        for (Movie m : movies) {
            movieMapper.insert(m);
        }
        log.info("已插入 {} 部种子电影", movies.size());
    }

    private Movie createMovie(Long tmdbId, String title, String origTitle, String posterPath,
                               String overview, LocalDate releaseDate, double rating, int votes,
                               int runtime, String genres, String director, String cast) {
        Movie m = new Movie();
        m.setTmdbId(tmdbId);
        m.setTitle(title);
        m.setOriginalTitle(origTitle);
        m.setPosterPath(posterPath);
        m.setOverview(overview);
        m.setReleaseDate(releaseDate);
        m.setVoteAverage(BigDecimal.valueOf(rating));
        m.setVoteCount(votes);
        m.setRuntime(runtime);
        m.setGenres(genres);
        m.setDirector(director);
        m.setCast(cast);
        return m;
    }
}
