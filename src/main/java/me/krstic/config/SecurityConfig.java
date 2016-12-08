package me.krstic.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;


@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
    protected void configure(HttpSecurity http) throws Exception {
        http
        	.authorizeRequests()
        		.anyRequest().fullyAuthenticated();
        http
        	.httpBasic();
        http
        	.csrf().disable();
        
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(HttpMethod.OPTIONS, "/**");
    }


	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		
		DefaultSpringSecurityContextSource contextSource = new DefaultSpringSecurityContextSource("ldap://dcucb07.hvbyu.com:389/dc=hvbyu,dc=com");
		contextSource.setUserDn("CN=App LDAP User,OU=UCB System Accounts,OU=UCB IT Infrastructure,DC=HVBYU,DC=com");
		contextSource.setPassword("100-tiLD@Pu$3r");
		contextSource.setReferral("follow");
		contextSource.afterPropertiesSet();
		
		auth
			.ldapAuthentication()
				.userSearchFilter("(&(sAMAccountName={0}))")
				.userSearchBase("")
				.contextSource(contextSource);
		
		auth.eraseCredentials(false);
	}

}
