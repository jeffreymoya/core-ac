local Utils = require "kong.plugins.checkpermission.utils"


local PermissionCheck = {
  VERSION  = "1.0.0",
  PRIORITY = 10,
}

function PermissionCheck:access(plugin_conf)
  local user = kong.request.get_header(plugin_conf.auth_user_header)
  local roles = kong.request.get_header(plugin_conf.auth_roles_header)
  local routePath = kong.request.get_path_with_query()
  local httpMethod = kong.request.get_method()

  if not user or not roles then
    return kong.response.exit(403, "Access denied", { ["Content-Type"] = "text/plain" })
  end

  local hasAccess = Utils.checkPermission(user, roles, routePath, httpMethod, plugin_conf)

  if not hasAccess then
    return kong.response.exit(403, "Access denied", { ["Content-Type"] = "text/plain" })
  end
end

return PermissionCheck
