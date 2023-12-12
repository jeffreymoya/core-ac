local PLUGIN_NAME = "checkpermission"


-- Lua unit testing is typically done with a library like Busted or luaunit.
-- Here's an example of how you might write these tests using Busted.

describe("PermissionCheck", function()
  local PermissionCheck = require "kong.plugins.checkpermission.handler"
  local Utils = require "kong.plugins.checkpermission.utils"

  before_each(function()
    -- Reset the Utils module before each test
    package.loaded["kong.plugins.checkpermission.utils"] = nil
    Utils = require "kong.plugins.checkpermission.utils"
    _G.kong = {
      request = {},
      service = {},
      response = {
        exit = function(status, content, headers)
          return {
            status = status,
            content = content,
            headers = headers
          }
        end
      },
      log = {
        err = function() end
      }
    }
  end)

  it("returns 401 Unauthorized when no Authorization header is present", function()
    kong.request.get_header = function() return nil end

    local response = PermissionCheck:access({})
    assert.same({ status = 401, content = "Unauthorized", headers = { ["Content-Type"] = "text/plain" } }, response)
  end)

  it("returns 401 Invalid JWT when JWT is invalid", function()
    kong.request.get_header = function() return "invalid_jwt" end

    local response = PermissionCheck:access({})
    assert.same({ status = 401, content = "Invalid JWT", headers = { ["Content-Type"] = "text/plain" } }, response)
  end)

  it("returns 403 Access denied when user does not have access", function()
    kong.request.get_header = function() return "valid_jwt" end
    Utils.extractJwt = function() return "user_id" end
    Utils.checkPermission = function() return false end

    local response = PermissionCheck:access({})
    assert.same({ status = 403, content = "Access denied", headers = { ["Content-Type"] = "text/plain" } }, response)
  end)

  it("sets the request path when user has access", function()
    kong.request.get_header = function() return "valid_jwt" end
    Utils.extractJwt = function() return "user_id" end
    Utils.checkPermission = function() return true end
    kong.service.request.set_path = function(path) assert.same("/your/forwarding/route", path) end

    PermissionCheck:access({ route_url = "/your/forwarding/route" })
  end)
end)
