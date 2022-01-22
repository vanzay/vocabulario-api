package vio.conf

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.filter.CommonsRequestLoggingFilter
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import vio.interceptors.AuthInterceptor
import javax.servlet.Filter

@Configuration
class MvcConfig(
    private val authInterceptor: AuthInterceptor
) : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
//            .allowedOrigins("*")
//            .allowedMethods("GET", "PUT", "POST", "PATCH", "DELETE", "OPTIONS")
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authInterceptor).addPathPatterns(
            "/v1/shelf/**",
            "/v1/training/**",
            "/v1/dictionary/**"
        )
    }

    @Bean
    fun webLoggingFilter(): FilterRegistrationBean<Filter> {
        val loggingFilter = CommonsRequestLoggingFilter()
        loggingFilter.setIncludeClientInfo(true)
        loggingFilter.setIncludeQueryString(true)
        loggingFilter.setIncludePayload(true)

        val registration = FilterRegistrationBean<Filter>()
        registration.filter = loggingFilter
        registration.addUrlPatterns("/*")
        return registration
    }
}
