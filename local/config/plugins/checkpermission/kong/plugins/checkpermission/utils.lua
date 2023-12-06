local http = require "resty.http"
local cjson = require "cjson.safe"

local Utils = {}

function Utils.checkPermission(user, routePath, httpMethod, plugin_conf)
  local httpc = http.new()
  httpc:set_timeout(5000)

  local res, err = httpc:request_uri(plugin_conf.pdp_endpoint, {
    method = "POST",
    headers = {
      ["Content-Type"] = "application/json",
      [plugin_conf.auth_user_header] = user,
    },
    body = cjson.encode({ path = routePath, httpMethod = httpMethod }),
  })

  if not res then
    kong.log.err("Failed to make API request: ", err)
    return false
  end

  -- Return false if the status code is 403
  if res.status == 403 then
    return false
  end

  return true
end

return Utils
