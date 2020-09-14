package com.spldeolin.allison1875.docanalyzer.processor;

import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.docanalyzer.DocAnalyzerConfig;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/**
 * 内聚了 对请求URL和请求动词解析的功能
 *
 * @author Deolin 2020-06-10
 */
class RequestMappingProc {

    private static final PathMatcher pathMatcher = new AntPathMatcher();

    private final String[] cPaths;

    private final RequestMethod[] cVerbs;

    @Getter
    private Collection<String> combinedUrls;

    @Getter
    private Collection<RequestMethod> combinedVerbs;

    public RequestMappingProc(Class<?> controllerClass) {
        RequestMapping controllerRequestMapping = findRequestMappingAnnoOrElseNull(controllerClass);
        cPaths = findValueFromAnno(controllerRequestMapping);
        cVerbs = findVerbFromAnno(controllerRequestMapping);
    }

    public void analyze(Method reflectionMethod) {
        RequestMapping methodRequestMapping = findRequestMappingAnnoOrElseNull(reflectionMethod);
        String[] mPaths = methodRequestMapping.value();
        RequestMethod[] mVerbs = methodRequestMapping.method();
        List<String> combinedUrls = combineUrl(cPaths, mPaths);
        this.combinedUrls = combinedUrls;
        combinedVerbs = combineVerb(cVerbs, mVerbs);

        // 添加全局前缀
        String globalUrlPrefix = DocAnalyzerConfig.getInstance().getGlobalUrlPrefix();
        if (StringUtils.isNotBlank(globalUrlPrefix)) {
            ListIterator<String> itr = combinedUrls.listIterator();
            while (itr.hasNext()) {
                itr.set(globalUrlPrefix + itr.next());
            }
        }
    }

    private RequestMapping findRequestMappingAnnoOrElseNull(AnnotatedElement annotated) {
        return AnnotatedElementUtils.findMergedAnnotation(annotated, RequestMapping.class);
    }


    private String[] findValueFromAnno(RequestMapping controllerRequestMapping) {
        return controllerRequestMapping == null ? new String[0] : controllerRequestMapping.value();
    }

    private RequestMethod[] findVerbFromAnno(RequestMapping controllerRequestMapping) {
        return controllerRequestMapping == null ? new RequestMethod[0] : controllerRequestMapping.method();
    }

    private Collection<RequestMethod> combineVerb(RequestMethod[] cVerbs, RequestMethod[] mVerbs) {
        Collection<RequestMethod> combinedVerbs = Lists.newArrayList();
        if (ArrayUtils.isNotEmpty(cVerbs)) {
            combinedVerbs.addAll(Arrays.asList(cVerbs));
        }
        if (ArrayUtils.isNotEmpty(mVerbs)) {
            combinedVerbs.addAll(Arrays.asList(mVerbs));
        }
        if (combinedVerbs.size() == 0) {
            combinedVerbs.addAll(Arrays.asList(RequestMethod.values()));
        }
        return combinedVerbs;
    }

    private List<String> combineUrl(String[] cPaths, String[] mPaths) {
        List<String> combinedUrls = Lists.newArrayList();
        if (ArrayUtils.isNotEmpty(cPaths) && ArrayUtils.isNotEmpty(mPaths)) {
            for (String cPath : cPaths) {
                for (String mPath : mPaths) {
                    combinedUrls.add(pathMatcher.combine(cPath, mPath));
                }
            }
        } else if (ArrayUtils.isEmpty(cPaths)) {
            combinedUrls.addAll(Arrays.asList(mPaths));
        } else if (ArrayUtils.isEmpty(mPaths)) {
            combinedUrls.addAll(Arrays.asList(cPaths));
        } else {
            combinedUrls.add("/");
        }
        combinedUrls = ensureAllStartWithSlash(combinedUrls);
        return combinedUrls;
    }

    private List<String> ensureAllStartWithSlash(Collection<String> urls) {
        List<String> result = Lists.newArrayList();
        for (String url : urls) {
            if (!url.startsWith("/")) {
                url = "/" + url;
            }
            result.add(url);
        }
        return result;
    }

}
