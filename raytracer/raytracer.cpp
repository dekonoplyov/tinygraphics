#include "geometry.h"

#include <fstream>
#include <string>
#include <vector>

const int WIDTH = 1280;
const int HEIGHT = 720;

void writePPM(const std::string& filepath, const std::vector<Vec3f>& framebuffer)
{
    std::ofstream ofs;
    ofs.open(filepath);
    // header
    ofs << "P6\n" << WIDTH << " " << HEIGHT << "\n255\n";

    for (const auto pixel : framebuffer) {
        for (size_t i = 0; i < 3; ++i) {
            ofs << static_cast<char>(255 * std::max(0.f, std::min(255.f, pixel[i])));
        }
    }
}

void render(const std::string& filepath)
{

    std::vector<Vec3f> framebuffer(WIDTH * HEIGHT);

    for (int j = 0; j < HEIGHT; ++j) {
        for (int i = 0; i < WIDTH; ++i) {
            framebuffer[i + j * WIDTH] = Vec3f{
                j / static_cast<float>(HEIGHT),
                i / static_cast<float>(WIDTH),
                0.f};
        }
    }

    writePPM(filepath, framebuffer);
}

int main()
{
    render("./raytracer/out.ppm");
    return 0;
}