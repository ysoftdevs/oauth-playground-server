package com.ysoft.geecon;

import com.ysoft.geecon.dto.Pkce;
import com.ysoft.geecon.repo.SessionsRepo;
import com.ysoft.geecon.repo.UsersRepo;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestQuery;

@Path("/admin")
public class AdminResource {
    @Inject
    UsersRepo usersRepo;
    @Inject
    SessionsRepo sessionsRepo;

    @GET
    @Path("reset")
    @Produces(MediaType.TEXT_HTML)
    public Response reset(@RestQuery("auth") String auth) {
        if (auth != null && Pkce.s256(auth).equals("gT8T_jmTnAI4KLIutKj8jLEPQA3oNYxDEp_IHaLGfxo")) {
            usersRepo.reset();
            sessionsRepo.reset();
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
}






