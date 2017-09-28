Thread = luajava.bindClass("java.lang.Thread")

local scriptDone = false

local clock = os.clock
function sleep(n)  -- seconds
--  local t0 = clock()
--  while clock() - t0 <= n do end
    Thread:sleep(n + 1000)
end

function stop()
    scriptDone = true    
end

function Main()
    io.write("Starting simple script\n")
    while not scriptDone do
        sleep(2)
        io.write("Once more please\n")
    end
    io.write("simple script done.\n")
end
