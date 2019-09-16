#include "geometry.h"

#include <fstream>
#include <limits>
#include <string>
#include <vector>

const int WIDTH = 1280;
const int HEIGHT = 720;
const int FOV = M_PI / 2.;

struct Material {
public:
    Material(const Vec3f& color)
        : diffuseColor(color)
    {
    }
    Material()
        : diffuseColor()
    {
    }

public:
    Vec3f diffuseColor;
};

struct Sphere {
public:
    Sphere(const Vec3f c, const float r, const Material m)
        : center{c}
        , radius{r}
        , material{m}
    {
    }

    bool rayIntersect(const Vec3f orig, const Vec3f dir, float& t0) const
    {
        const auto L = center - orig;
        const float tca = L * dir;
        const float d2 = L * L - tca * tca;
        if (d2 > radius) {
            return false;
        }
        const float thc = sqrtf(radius * radius - d2);
        t0 = tca - thc;
        const float t1 = tca + thc;
        if (t0 < 0) {
            t0 = t1;
            return false;
        }
        return true;
    }

public:
    Vec3f center;
    float radius;
    Material material;
};

void writePPM(const std::string& filepath, const std::vector<Vec3f>& framebuffer)
{
    std::ofstream ofs;
    ofs.open(filepath);
    // header
    ofs << "P6\n"
        << WIDTH << " " << HEIGHT << "\n255\n";

    for (const auto pixel : framebuffer) {
        for (size_t i = 0; i < 3; ++i) {
            ofs << static_cast<char>(255 * std::max(0.f, std::min(255.f, pixel[i])));
        }
    }
}

bool sceneIntersect(const Vec3f& orig, const Vec3f& dir,
    const std::vector<Sphere>& spheres,
    Vec3f& hit, Vec3f& N, Material& material)
{
    float spheresDist = std::numeric_limits<float>::max();
    for (const auto& sphere : spheres) {
        float dist = 0.f;
        if (sphere.rayIntersect(orig, dir, dist) && dist < spheresDist) {
            spheresDist = dist;
            hit = orig + dir * dist;
            N = (hit - sphere.center).normalize();
            material = sphere.material;
        }
    }

    return spheresDist < 1000.f;
}

Vec3f castRay(const Vec3f& orig, const Vec3f& dir, const std::vector<Sphere>& spheres)
{
    Vec3f point, N;
    Material material;

    if (!sceneIntersect(orig, dir, spheres, point, N, material)) {
        return Vec3f(0.2, 0.7, 0.8); // background color
    }
    return material.diffuseColor;
}

void render(const std::string& filepath, const std::vector<Sphere>& spheres)
{
    std::vector<Vec3f> framebuffer(WIDTH * HEIGHT);

    for (int j = 0; j < HEIGHT; ++j) {
        for (int i = 0; i < WIDTH; ++i) {
            float x = (i + 0.5) - WIDTH / 2.;
            float y = -(j + 0.5) + HEIGHT / 2.;
            float z = -HEIGHT / (2. * tan(FOV / 2.));
            Vec3f dir = Vec3f(x, y, z).normalize();
            framebuffer[i + j * WIDTH] = castRay(Vec3f(0, 0, 0), dir, spheres);
        }
    }

    writePPM(filepath, framebuffer);
}

int main()
{
    Material ivory(Vec3f(0.4, 0.4, 0.3));
    Material redRubber(Vec3f(0.3, 0.1, 0.1));

    std::vector<Sphere> spheres;
    spheres.push_back(Sphere(Vec3f(-3, 0, -16), 2, ivory));
    spheres.push_back(Sphere(Vec3f(-1.0, -1.5, -12), 2, redRubber));
    spheres.push_back(Sphere(Vec3f(1.5, -0.5, -18), 3, redRubber));
    spheres.push_back(Sphere(Vec3f(7, 5, -18), 4, ivory));

    render("./raytracer/out.ppm", spheres);
    return 0;
}