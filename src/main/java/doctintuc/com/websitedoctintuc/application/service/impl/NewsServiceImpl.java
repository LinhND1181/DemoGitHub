package doctintuc.com.websitedoctintuc.application.service.impl;

import doctintuc.com.websitedoctintuc.application.constants.CommonConstant;
import doctintuc.com.websitedoctintuc.application.constants.DevMessageConstant;
import doctintuc.com.websitedoctintuc.application.jwt.JwtUtils;
import doctintuc.com.websitedoctintuc.application.repository.CategoryRepository;
import doctintuc.com.websitedoctintuc.application.repository.NewsRepository;
import doctintuc.com.websitedoctintuc.application.repository.UserNewsRepository;
import doctintuc.com.websitedoctintuc.application.repository.UserRepository;
import doctintuc.com.websitedoctintuc.application.request.UserNewsRequest;
import doctintuc.com.websitedoctintuc.application.service.INewsService;
import doctintuc.com.websitedoctintuc.config.exception.VsException;
import doctintuc.com.websitedoctintuc.domain.dto.CustomNewDTO;
import doctintuc.com.websitedoctintuc.domain.dto.NewsDTO;
import doctintuc.com.websitedoctintuc.domain.entity.*;
import doctintuc.com.websitedoctintuc.domain.pagine.PaginateDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements INewsService {

    private final static Logger logger = LoggerFactory.getLogger(NewsServiceImpl.class);
    private final NewsRepository newsRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final UserNewsRepository userNewsRepository;

    @Override
    public News create(NewsDTO newsDTO, HttpServletRequest request) {
        if (newsRepository.existsByTitle(newsDTO.getTitle())) {
            throw new VsException(DevMessageConstant.Common.DUPLICATE_NAME, newsDTO.getTitle());
        }
        Optional<Category> category = categoryRepository.findById(newsDTO.getCategoryId());
        if (category.isEmpty()) {
            throw new VsException(String.format(DevMessageConstant.Common.NOT_FOUND_OBJECT_BY_ID,
                    CommonConstant.ClassName.CATEGORY_CLASS_NAME, newsDTO.getCategoryId()));
        }
        String authToken = request.getHeader("Authorization").substring(7);
        String username = jwtUtils.getUserByToken(authToken);
        User user = userRepository.findByUsername(username);
        News news = new News(
                newsDTO.getTitle(),
                newsDTO.getContent(),
                newsDTO.getAuthor(),
                newsDTO.getDescription(),
                newsDTO.getThumbnail());
        news.setCategory(category.get());
        news.setCreateBy(user.getFullName());
        news.setLastModifiedBy(user.getFullName());
        return newsRepository.save(news);
    }

    @Override
    public News get(Integer id) {
        if (!newsRepository.existsById(id)) {
            throw new VsException(String.format(DevMessageConstant.Common.NOT_FOUND_OBJECT_BY_ID,
                    CommonConstant.ClassName.NEWS_CLASS_NAME, id));
        }
        return newsRepository.findById(id).get();
    }

    @Override
    public News update(Integer id, NewsDTO newsDTO, HttpServletRequest request) {

        Optional<News> foundNews = newsRepository.findById(id);
        if (foundNews.isPresent()) {
            if (newsRepository.existsByTitle(newsDTO.getTitle())) {
                throw new VsException(DevMessageConstant.Common.DUPLICATE_NAME, newsDTO.getTitle());
            }
            Optional<Category> category = categoryRepository.findById(newsDTO.getCategoryId());
            if (category.isEmpty()) {
                throw new VsException(String.format(DevMessageConstant.Common.NOT_FOUND_OBJECT_BY_ID,
                        CommonConstant.ClassName.CATEGORY_CLASS_NAME, newsDTO.getCategoryId()));
            }
            String authToken = request.getHeader("Authorization").substring(7);
            String username = jwtUtils.getUserByToken(authToken);
            User user = userRepository.findByUsername(username);

            News news = new News(
                    newsDTO.getTitle(),
                    newsDTO.getContent(),
                    newsDTO.getAuthor(),
                    newsDTO.getDescription(),
                    newsDTO.getThumbnail()
            );
            news.setId(id);
            news.setCreateBy(foundNews.get().getCreateBy());
            news.setLastModifiedBy(user.getFullName());
            news.setCategory(category.get());
            return newsRepository.save(news);
        } else {
            throw new VsException(String.format(DevMessageConstant.Common.NOT_FOUND_OBJECT_BY_ID,
                    CommonConstant.ClassName.NEWS_CLASS_NAME, id));
        }
    }

    @Override
    public PaginateDTO<News> searchAll(Integer page, Integer size) {
        int totalPage = (int) Math.ceil((double) newsRepository.count() / size);
        return new PaginateDTO<>(
                newsRepository.findAll(PageRequest.of(page, size, Sort.by(CommonConstant.SORT_BY_TIME2).descending()))
                        .getContent(), page, totalPage);
    }

    @Override
    public List<News> searAllNotPaginate() {
        if (ObjectUtils.isEmpty(newsRepository.findAll())) {
            throw new VsException(DevMessageConstant.Common.NO_DATA_SELECTED);
        }
        return newsRepository.findAll();
    }

    @Override
    public String delete(Integer id) {
        if (!newsRepository.existsById(id)) {
            throw new VsException(String.format(DevMessageConstant.Common.NOT_FOUND_OBJECT_BY_ID,
                    CommonConstant.ClassName.NEWS_CLASS_NAME, id));
        }
        newsRepository.deleteById(id);
        return DevMessageConstant.Common.NOTIFICATION_DELETE_SUCCESS;
    }

    @Override
    public List<News> getFavoriteNews() {
        if (ObjectUtils.isEmpty(newsRepository.findAll())) {
            throw new VsException(DevMessageConstant.Common.NO_DATA_SELECTED);
        }

        return newsRepository.favoriteNews();
    }

    @Override
    public List<News> getLeastNews() {
        if (ObjectUtils.isEmpty(newsRepository.findAll())) {
            throw new VsException(DevMessageConstant.Common.NO_DATA_SELECTED);
        }
        return newsRepository.leastNews();
    }

    @Override
    public PaginateDTO<News> paginateHomePage(Integer page, Integer size) {
        if (ObjectUtils.isEmpty(newsRepository.findAll())) {
            throw new VsException(DevMessageConstant.Common.NO_DATA_SELECTED);
        }
        return new PaginateDTO<>(newsRepository.findAll(PageRequest.of(page, size,
                Sort.by(CommonConstant.SORT_BY_TIME2).descending())).getContent(), page, size);
    }

    @Override
    public News setView(Integer id) {
        if (!newsRepository.existsById(id)) {
            throw new VsException(String.format(DevMessageConstant.Common.NOT_FOUND_OBJECT_BY_ID,
                    CommonConstant.ClassName.NEWS_CLASS_NAME, id));
        }
        News news = newsRepository.getById(id);
        news.setView(news.getView() + 1);
        return newsRepository.save(news);
    }

    @Override
    public CustomNewDTO filterNewsByCategory(Integer page, Integer size, String author, String title, Integer categoryId, String filter) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new VsException(String.format(DevMessageConstant.Common.NOT_FOUND_OBJECT_BY_ID,
                    CommonConstant.ClassName.CATEGORY_CLASS_NAME, categoryId));
        }
        List<News> listNewByCategory = new ArrayList<>();
        int totalPage = 0;
        if (!StringUtils.hasText(filter) || filter.equalsIgnoreCase(CommonConstant.SORT_ASC)) {
            List<News> listNews1 = newsRepository.filterNewsByCategory(categoryId, title, author, PageRequest.of(page, size,
                    Sort.by(CommonConstant.SORT_BY_TIME).ascending()));
            for (News item : listNews1) {
                listNewByCategory.add(new News(
                        item.getId(), item.getTitle(), item.getContent(),
                        item.getAuthor(), item.getDescription(),
                        item.getThumbnail(), item.getView()));
            }
            totalPage = (int) Math.ceil((double) listNews1.size() / size);
        }
        if (StringUtils.hasText(filter) && filter.equalsIgnoreCase(CommonConstant.SORT_DESC)) {
            List<News> listNews2 = newsRepository.filterNewsByCategory(categoryId, title, author, PageRequest.of(page, size,
                    Sort.by(CommonConstant.SORT_BY_TIME).descending()));
            for (News item : listNews2) {
                listNewByCategory.add(new News(
                        item.getId(), item.getTitle(), item.getContent(),
                        item.getAuthor(), item.getDescription(),
                        item.getThumbnail(), item.getView()));
            }
            totalPage = (int) Math.ceil((double) listNews2.size() / size);
        }
        return new CustomNewDTO(listNewByCategory, categoryRepository.findById(categoryId).get(), totalPage);
    }

    @Override
    public PaginateDTO<News> searchNews(Integer page, Integer size, String key) {
        if (!StringUtils.hasText(key)) {
            throw new VsException(DevMessageConstant.Common.NO_DATA_SELECTED);
        }
        return new PaginateDTO<>(newsRepository.searchNewsByKey("%" + key.toUpperCase(Locale.ROOT).trim() + "%",
                PageRequest.of(page, size)), page, size);
    }

    @Override
    public Integer countRecordNews() {
        if (ObjectUtils.isEmpty(newsRepository.findAll())) {
            return 0;
        }
        return newsRepository.countRecordNews();
    }

    @Override
    public News saveNewsWatched(UserNewsRequest request) {
        if (!newsRepository.existsById(request.getNewsId())) {
            throw new VsException(String.format(DevMessageConstant.Common.NOT_FOUND_OBJECT_BY_ID,
                    CommonConstant.ClassName.NEWS_CLASS_NAME, request.getNewsId()));
        } else {
            News news = newsRepository.findById(request.getNewsId()).get();
            if (!userRepository.existsById(request.getUserId())) {
                news.setView(news.getView() + 1);
                return news;
            } else {
                news.setView(news.getView() + 1);
                RatingKey ratingKey = new RatingKey(request.getUserId(), request.getNewsId());
                User user = userRepository.findById(request.getUserId()).get();
                UserNews userNews = new UserNews(ratingKey, user, news);
                userNewsRepository.save(userNews);
                return news;
            }
        }
    }

    @Override
    public List<News> getAllNewWatched(UserNewsRequest request) {
        if (!newsRepository.existsById(request.getNewsId())) {
            throw new VsException(String.format(DevMessageConstant.Common.NOT_FOUND_OBJECT_BY_ID,
                    CommonConstant.ClassName.NEWS_CLASS_NAME, request.getNewsId()));
        } else {
            List<UserNews> userNews = userNewsRepository.findAll();
            List<News> listNews = new ArrayList<>();
            for (UserNews item : userNews) {
                if (item.getUser().getId() == request.getUserId()) {
                    News news = newsRepository.findById(item.getNews().getId()).get();
                    listNews.add(news);
                }
            }
            if (listNews.isEmpty()) {
                throw new VsException(DevMessageConstant.Common.NO_DATA_SELECTED);
            } else {
                return listNews;
            }
        }
    }
}
