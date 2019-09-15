add_executable(ray raytracer.cpp)
set_target_properties(ray PROPERTIES CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3")