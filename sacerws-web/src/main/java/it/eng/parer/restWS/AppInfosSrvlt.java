/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna <p/> This program is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version. <p/> This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. <p/> You should
 * have received a copy of the GNU Affero General Public License along with this program. If not,
 * see <https://www.gnu.org/licenses/>.
 */

package it.eng.parer.restWS;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.ejb.ControlliWS;
import it.eng.parer.ws.utils.Costanti;

@WebServlet(urlPatterns = {
        "/admin/infos" }, asyncSupported = true)
public class AppInfosSrvlt extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(AppInfosSrvlt.class);

    // constant
    private static final String ROOT_GIT = "git";
    private static final String ROOT_ENV = "env";
    private static final String ROOT_SYSPROPS = "sysprops";
    //
    private static final String SYS_CONFIG_ROOT_TO_SKIP = "admin-appinfos.roottoskip";
    private static final String SYS_CONFIG_PROP_TO_SKIP = "admin-appinfos.proptoskip";

    private Properties gitproperties = null;

    @EJB
    private ControlliWS myControlliWs;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        // custom
        try (InputStream input = getClass().getResourceAsStream("/git.properties")) {
            gitproperties = new Properties();
            // load a properties file
            gitproperties.load(input);
        } catch (IOException e) {
            log.error("Errore init", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!isUserAuthenticated(req.getHeader("authorization"), getIpAddrFromReq(req))) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        } else {
            // json mapper
            ObjectMapper mapper = new ObjectMapper();
            // final result
            final Map<String, Map<String, String>> infos = Collections
                    .synchronizedMap(new LinkedHashMap<>());
            // props or root to skip
            final String rootToSkip = Objects.toString(System.getProperty(SYS_CONFIG_ROOT_TO_SKIP),
                    StringUtils.EMPTY);
            final String propToSkip = Objects.toString(System.getProperty(SYS_CONFIG_PROP_TO_SKIP),
                    StringUtils.EMPTY);
            // infos
            // git
            Map<String, String> git = gitproperties.entrySet().stream()
                    .filter(p -> !String.valueOf(p.getKey()).matches(propToSkip))
                    .collect(Collectors.toMap(e -> String.valueOf(e.getKey()),
                            e -> String.valueOf(e.getValue()), (prev, next) -> next, HashMap::new));
            // filter
            if (!ROOT_GIT.matches(rootToSkip)) {
                infos.put(ROOT_GIT, git);
            }
            // env
            Map<String, String> env = System.getenv().entrySet().stream()
                    .filter(p -> !p.getKey().matches(propToSkip))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            // filter
            if (!ROOT_ENV.matches(rootToSkip)) {
                infos.put(ROOT_ENV, env);
            }
            // sys props
            Map<String, String> sysprops = System.getProperties().entrySet().stream()
                    .filter(p -> !String.valueOf(p.getKey()).matches(propToSkip))
                    .collect(Collectors.toMap(e -> String.valueOf(e.getKey()),
                            e -> String.valueOf(e.getValue()), (prev, next) -> next, HashMap::new));
            // filter
            if (!ROOT_SYSPROPS.matches(rootToSkip)) {
                infos.put(ROOT_SYSPROPS, sysprops);
            }

            // fixed
            resp.setStatus(HttpServletResponse.SC_OK);
            try {
                resp.setContentType("application/json; charset=\"utf-8\"");
                resp.getWriter().println(mapper.writeValueAsString(infos));
            } catch (IOException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                log.error("Errore generico", e);
            }
        }
    }

    /*
     * Basic Auth con credenziali da anagrafica. Nota: si ri-utilizza per semplificare il processo
     * di gestione, il servizio di monitoraggio che è comunque da considerasi ad utilizzo esclusivo
     * interno / amministrativo.
     */
    private boolean isUserAuthenticated(String authString, String ipAddr) {
        if (StringUtils.isBlank(authString))
            return false;
        String decodedAuth = "";
        // Header is in the format "Basic 5tyc0uiDat4"
        // We need to extract data before decoding it back to original string
        String[] authParts = authString.split("\\s+");
        String authInfo = authParts[1];
        // Decode the data back to original string
        byte[] bytes = null;
        bytes = new Base64().decode(authInfo);
        decodedAuth = new String(bytes);

        final String AUTH_SPLITTERATOR = ":";
        final String loginName = decodedAuth.split(AUTH_SPLITTERATOR)[0];
        final String pwd = decodedAuth.split(AUTH_SPLITTERATOR)[1];

        // check credentials
        RispostaControlli rs = myControlliWs.checkCredenziali(loginName, pwd, ipAddr,
                Costanti.TipiWSPerControlli.VERSAMENTO_RECUPERO);

        return rs.isrBoolean();
    }

    private String getIpAddrFromReq(HttpServletRequest request) {
        String ipVers = request.getHeader("RERFwFor");
        // cerco l'header custom della RER
        if (ipVers == null || ipVers.isEmpty()) {
            ipVers = request.getHeader("X-FORWARDED-FOR");
            // se non c'e`, uso l'header standard
        }
        if (ipVers == null || ipVers.isEmpty()) {
            ipVers = request.getRemoteAddr();
            // se non c'e` perche' la macchina e' esposta direttamente,
            // leggo l'IP fisico del chiamante
        }
        log.info("Request, indirizzo di provenienza - IP:  {}", ipVers);
        return ipVers;
    }
}
