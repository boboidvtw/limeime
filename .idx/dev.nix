# To learn more about how to use Nix to configure your environment
# see: https://developers.google.com/idx/guides/customize-idx-env

{ pkgs, ... }: {
  # Which nixpkgs channel to use.
  channel = "stable-23.11"; # or "unstable"

  # Use https://search.nixos.org/packages to find packages
  packages = [
    pkgs.jdk17
    pkgs.gradle
  ];

  # Sets environment variables in the workspace
  env = {};

  idx = {
    # Search for the extensions you want on https://open-vsx.org/
    extensions = [
      "vscjava.vscode-java-pack"
    ];

    # Workspace lifecycle hooks
    workspace = {
      onCreate = {
        build-app = "cd LimeStudio && ./gradlew assembleDebug";
      };
      onStart = {};
    };
  };
}
